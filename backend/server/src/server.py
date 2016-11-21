from flask import Flask, request, jsonify
import requests
import couchdb
import json

application = Flask(__name__)
couch = couchdb.Server('http://dev:pronto@0.0.0.0:5984')
fb_key = "1117295381688482|EwDDv3rzCr5C-9QwpSm6qkE-7L8"

######################################################################
# Utilities
######################################################################
def get_user_name(user_id, token):
    graph_url = "https://graph.facebook.com/"
    r = requests.get("{}{}".format(graph_url, user_id), params={"access_token": token})
    # print(r.text)
    json_data = json.loads(r.text)

    return json_data.get('name', 'Name not found!')

def download_text(url):
    r = requests.get(url)
    if r.status_code != 200:
        return "Error!"

    return r.text

def verify_token(token, api_token):
    graph_url = "https://graph.facebook.com/me/"
    r = requests.get("{}".format(graph_url), params={"access_token": token})
    json_data = json.loads(r.text)

    fb_id = json_data.get('id', None)
    return fb_id

def get_user_groups(user_id):
    db = couch['users']
    groups_search = db.iterview('userUtil/usersGroups', 20, startkey=user_id, endkey=user_id)
    groups = [g.value for g in groups_search]

    return groups

def id_to_name(msgs):
    # db = couch['users']
    # gen = db.iterview('userUtil/getName', 20)
    # lookup = {row.key: row.value for row in gen}
    # lookup = [(u.key, u.value) for u in gen]

    # print(lookup)
    users = []
    for m in msgs:
        if m['author_id'] in users:
            users.append(m['author_id'])
        
    # Get all users info
    graph_url = "https://graph.facebook.com/?ids="
    for msg in msgs:
        graph_url += msg['author_id'] + ','

    graph_url = graph_url[:-1]
    graph_url += '&access_token=' + fb_key

    user_data = json.loads(download_text(graph_url))
    for msg in msgs:
        msg['author_id'] = user_data[msg['author_id']]['name']

        # print(m)
        # print(m['author_id'])

        # m['author_id'] = lookup.get(m.get('author_id'), 'Name not found!')
        # print(m['author_id'])
        
        # m['author_id'] = lookup[m['author_id']]
        

    return msgs

######################################################################
# VIEWS
######################################################################
@application.route("/")
def hello():
    return "<h1 style='color:blue'>Hello There!</h1>"

@application.route('/login/', methods=['POST'])
def login():
    access_token = request.form.get('access_token', None)
    if access_token is None:
        return 'Error'

    response = {'status': 'ok'}    

    fb_id = verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        return jsonify(response)

    response['groups'] = get_user_groups(fb_id)
    
    return jsonify(response)

@application.route('/msg/like/', methods=['POST'])
def like_msg():
    msg_id = request.form.get('msg_id', '')
    access_token = request.form.get('access_token', None)
    # group_id = request.args.get('group_id', '')
    
    response = {'status': 'ok'}    
    fb_id = verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid token!'
        return jsonify(response)

    db = couch['messages']
    msg = db[msg_id]

    # print(msg['group_id'], get_user_groups(fb_id))

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in get_user_groups(fb_id)[0]: # Uses 0 because it is an [] of [] by accident
        # Old messages have like as an integer
        if msg['likes'] is not list:
            msg['likes'] = []

        print(msg)

        # Unlike if already liked
        if fb_id in msg['likes']:
            msg['likes'].remove(fb_id)
            db.save(msg)

            response['error-msg'] = 'Unliked!'
            return jsonify(response)

        # Add the user to the likes list
        msg['likes'].append(fb_id)
        response['error-msg'] = 'Liked!'
        
        db.save(msg)

        return jsonify(response)
    else:
        response['status'] = 'error'
        response['error-msg'] = 'Not in group!'
        return jsonify(response)

@application.route('/msg/dislikes/', methods=['POST'])
def dislike_msg():
    msg_id = request.form.get('msg_id', '')
    access_token = request.form.get('access_token', None)
    # group_id = request.args.get('group_id', '')
    
    response = {'status': 'ok'}    
    fb_id = verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid token!'
        return jsonify(response)

    db = couch['messages']
    msg = db[msg_id]

    # print(msg['group_id'], get_user_groups(fb_id))

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in get_user_groups(fb_id)[0]: # Uses 0 because it is an [] of [] by accident
        # Old messages have like as an integer
        if msg['dislikes'] is not list:
            msg['dislikes'] = []

        # Dislike should not be called more than once
        if fb_id in msg['dislikes']:
            response['status'] = 'error'
            response['error-msg'] = 'Already disliked!'

            return jsonify(response)
        
        # Add the user to the likes list
        msg['dislikes'].append(fb_id)
        db.save(msg)

        return jsonify(response)
    else:
        response['status'] = 'error'
        response['error-msg'] = 'Not in group!'
        return jsonify(response)

@application.route('/msg/bookmark/', methods=['POST'])
def bookmark_msg():
    pass

@application.route("/inbox/search/", methods=['GET'])
def search_msgs():
    try:
        # TODO: add authentication check
        group_id = request.args.get('group_id')
        access_token = request.args.get('access_token', None)
        max_messages = int(request.args.get('max_messages', 0))
        start_msg = request.args.get('start', None)
        
        keyword = request.args.get('keyword')
        print(keyword)
        # keyword = json.loads(request.args.get('keyword', '{[]}'))
        # print(keywords)

        # Limit the number of returned messages per queue
        max_messages = min(max_messages, 30)
        db = couch['messages']

        # If no starting message is provided, start with the newest
        gen = None
        if start_msg is not None:
            gen = db.iterview('chats/getMsgsRanks', 20, startkey=start_msg, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        else:
            gen = db.iterview('chats/getMsgsRanks', 20, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')        
        msgs = [m.value for m in gen]
        # for m  in msgs:
        # msgs = json.loads(msgs)

        # Rank the relevance of messages based on the frequency of keywords
        for msg in msgs:
            # for key in keywords:
            # print(msg['text'])
            msg['score'] += int(msg['text'].count(keyword))
            # if msg['score'] > 0:
                # print(msg)

        # Sort the msgs in descending order
        msgs = sorted(msgs, key=lambda msg: msg['score'], reverse=True)
        msgs = msgs[:max_messages]
        # print(msgs)

        # return 'Hi'
        # return str(msgs)
        return jsonify(msgs)

    except Exception as e:
        return 'Invalid Parameters'
        # raise e

@application.route("/inbox/main/", methods=['GET'])
def get_msgs():
    try:
        # TODO: add authentication check
        group_id = request.args.get('group_id')
        access_token = request.args.get('access_token', None)
        max_messages = int(request.args.get('max_messages', 0))
        start_msg = request.args.get('start', None)

        # Limit the number of returned messages per queue
        max_messages = min(max_messages, 30)
        db = couch['messages']

        # If no starting message is provided, start with the newest
        gen = None
        if start_msg is not None:
            gen = db.iterview('chats/getGroupMsgs', 20, limit=max_messages, startkey=start_msg, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        else:
            gen = db.iterview('chats/getGroupMsgs', 20, limit=max_messages, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        msgs = [m.value for m in gen]
        msgs = id_to_name(msgs)

        return jsonify(msgs)

    except Exception as e:
        raise e

if __name__ == "__main__":
    application.debug = True
    application.run(host='0.0.0.0')
