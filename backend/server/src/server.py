from flask import Flask, request, jsonify
import requests
import couchdb
import json

application = Flask(__name__)
couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')
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

# TODO: remove api_token field
def verify_token(token, api_token):
    graph_url = "https://graph.facebook.com/me/"
    r = requests.get("{}".format(graph_url), params={"access_token": token})
    json_data = json.loads(r.text)
    # print(json_data)

    fb_id = json_data.get('id', None)
    return fb_id

def get_user_groups(user_id):
    db = couch['users']
    groups_search = db.iterview('userUtil/usersGroups', 20, startkey=user_id, endkey=user_id)
    groups = [g.value for g in groups_search]

    return groups

def id_to_name(msgs):
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

    return msgs

# Find the user_id associated with an app id
def user_id_to_app_id(app_id):
    db = couch['users']
    users_search = db.iterview('userUtil/convertID', 5, startkey=app_id, endkey=app_id)
    user_id = next(users_search).value
    return user_id

######################################################################
# VIEWS
######################################################################
@application.route("/")
def hello():
    return "<h1 style='color:blue'>Hello There!</h1>"

@application.route('/login/', methods=['POST'])
def login():
    response = {'status': 'ok'}    

    access_token = request.form.get('access_token', None)
    print('Token:', access_token)

    if access_token is None:
        response['status'] = 'error'
        return jsonify(response)

    fb_id = verify_token(access_token, fb_key)
    # print(fb_id)
    if fb_id is None:
        response['status'] = 'error'
        return jsonify(response)
    
    name = get_user_name(fb_id, access_token)
    print('Name:', name)

    # Add the user's app_id to their document
    db = couch['users']
    users_search = db.iterview('userUtil/findUserName', 5, startkey=name, endkey=name)
    user_real_id = [g.value for g in users_search]
    if len(user_real_id) == 0:
        print('User\'s name not found!')
        response['error-msg'] = 'Name not found!'
        return jsonify(response)
    
    user = db[user_real_id[0]]
    user['user_id'] = fb_id
    db.save(user)

    response['groups'] = get_user_groups(db[user_real_id[0]]['id'])[0]
    print(response['groups'])

    db.commit()

    # print('ID Conversion:', user_id_to_app_id(fb_id))

    return jsonify(response)

@application.route('/msg/like/', methods=['POST'])
def like_msg():
    response = {'status': 'ok'}    

    msg_id = request.form.get('msg_id', '')
    access_token = request.form.get('access_token', None)
    if access_token is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid token!'
    # group_id = request.args.get('group_id', '')
    
    fb_id = verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid FB ID!'
        return jsonify(response)

    db = couch['messages']
    msg = db[msg_id]
    group_id = msg['group_id']
    print(msg_id)

    # Check if the user is in the group
    # if group_id not in get_user_groups(fb_id):
    #     response['status'] = 'error'
    #     response['error-msg'] = 'You are not in the group!'
    #     return jsonify(response)

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in get_user_groups(user_id_to_app_id(fb_id))[0]: # Uses 0 because it is an [] of [] by accident

        # Old messages have like as an integer
        if type(msg['likes']) is not list:
            print('Reset likes')
            msg['likes'] = []

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

@application.route('/msg/dislike/', methods=['POST'])
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

    # Check if the user is in the group
    # if fb_id not in get_user_groups(fb_id):
    #     response['status'] = 'error'
    #     response['error-msg'] = 'You are not in the group!'
    #     return jsonify(response)
    

    db = couch['messages']
    msg = db[msg_id]

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in get_user_groups(user_id_to_app_id(fb_id))[0]: # Uses 0 because it is an [] of [] by accident
        # Old messages have like as an integer
        if type(msg['dislikes']) is not list:
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
    response = {'status': 'ok'}    

    msg_id = request.form.get('msg_id', '')
    access_token = request.form.get('access_token', None)
    if access_token is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid token!'
    # group_id = request.args.get('group_id', '')
    
    fb_id = verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid FB ID!'
        return jsonify(response)

    db = couch['messages']
    msg = db[msg_id]

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in get_user_groups(user_id_to_app_id(fb_id))[0]: # Uses 0 because it is an [] of [] by accident

        # Old messages have like as an integer
        if type(msg['bookmarks']) is not list:
            print('Reset likes')
            msg['bookmarks'] = []

        # Unlike if already liked
        if fb_id in msg['bookmarks']:
            msg['bookmarks'].remove(fb_id)
            db.save(msg)

            response['error-msg'] = 'Unbookmarked!'
            return jsonify(response)

        # Add the user to the likes list
        msg['bookmarks'].append(fb_id)
        response['bookmarks'] = 'Bookmarked!'
        
        db.save(msg)

        return jsonify(response)
    else:
        response['status'] = 'error'
        response['error-msg'] = 'Not in group!'
        return jsonify(response)

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

        response = {'status': 'ok'}    

        # Check if token is valid
        fb_id = verify_token(access_token, fb_key)
        if fb_id is None:
            response['status'] = 'error'
            response['error-msg'] = 'Invalid FB ID!'
            return jsonify(response)

        # Check if the user is in the group
        print(get_user_groups(user_id_to_app_id(fb_id)))
        if group_id not in get_user_groups(user_id_to_app_id(fb_id))[0]:
            response['status'] = 'error'
            response['error-msg'] = 'You are not in the group!'
            return jsonify(response)

        # Limit the number of returned messages per queue
        max_messages = min(max_messages, 100)
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
            if 'score' not in msg:
                msgs.remove(msg)
                continue

            msg['score'] += int(msg['text'].count(keyword))
            # if msg['score'] > 0:
                # print(msg)

        print(msgs)

        # Sort the msgs in descending order
        msgs = sorted(msgs, key=lambda msg: msg.get('score', -10), reverse=True)
        msgs = msgs[:max_messages]
        # print(msgs)

        # return 'Hi'
        # return str(msgs)
        return jsonify(msgs)

    except Exception as e:
        raise e
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
        max_messages = min(max_messages, 100)
        db = couch['messages']

        response = {'status': 'ok'}    

        # Check if token is valid
        fb_id = verify_token(access_token, fb_key)
        if fb_id is None:
            response['status'] = 'error'
            response['error-msg'] = 'Invalid FB ID!'
            return jsonify(response)

        # Check if the user is in the group
        groups = get_user_groups(fb_id)
        print(groups)

        if group_id not in get_user_groups(user_id_to_app_id(fb_id))[0]:
            response['status'] = 'error'
            response['error-msg'] = 'You are not in the group!'
            return jsonify(response)

        # If no starting message is provided, start with the newest
        # TODO: Add check for specific group
        gen = None
        if start_msg is not None:
            gen = db.iterview('chats/getGroupMsgs', 20, limit=max_messages, startkey=start_msg, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        else:
            gen = db.iterview('chats/getGroupMsgs', 20, limit=max_messages, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        msgs = [m.value for m in gen if fb_id not in m.value['dislikes']]
        print(msgs)
        msgs = id_to_name(msgs)

        return jsonify(msgs)

    except Exception as e:
        raise e

if __name__ == "__main__":
    application.debug = True
    application.run(host='0.0.0.0')
