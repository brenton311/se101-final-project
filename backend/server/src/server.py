from flask import Flask, request, jsonify
import requests
import math
import couchdb
import json

import utils

application = Flask(__name__)
couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')
fb_key = "1117295381688482|EwDDv3rzCr5C-9QwpSm6qkE-7L8"

######################################################################
# VIEWS
######################################################################
@application.route('/')
def hello():
    return "<h1 style='color:blue'>Hello There!</h1>"

@application.route('/test-file/')
def file_test():
    return "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

@application.route('/login/', methods=['POST'])
def login():
    response = {'status': 'ok'}    

    access_token = request.form.get('access_token', None)
    print('Token:', access_token)

    if access_token is None:
        response['status'] = 'error'
        return jsonify(response)

    fb_id = utils.verify_token(access_token, fb_key)
    # print(fb_id)
    if fb_id is None:
        response['status'] = 'error'
        return jsonify(response)
    
    name = utils.get_user_name(fb_id, access_token)
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

    response['groups'] = utils.get_user_groups(db[user_real_id[0]]['id'])[0]
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
    
    fb_id = utils.verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid FB ID!'
        return jsonify(response)

    db = couch['msg_1150546131643551']
    msg = db[msg_id]
    group_id = msg['group_id']
    print(msg_id)

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in utils.get_user_groups(utils.user_id_to_app_id(fb_id))[0]: # Uses 0 because it is an [] of [] by accident

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
    
    response = {'status': 'ok'}    
    fb_id = utils.verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid token!'
        return jsonify(response)

    db = couch['msg_1150546131643551']
    msg = db[msg_id]

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in utils.get_user_groups(utils.user_id_to_app_id(fb_id))[0]: # Uses 0 because it is an [] of [] by accident
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
    
    fb_id = utils.verify_token(access_token, fb_key)
    if fb_id is None:
        response['status'] = 'error'
        response['error-msg'] = 'Invalid FB ID!'
        return jsonify(response)

    db = couch['msg_1150546131643551']
    msg = db[msg_id]

    # Make sure the user is in the group where the message 
    # is published
    if msg['group_id'] in utils.get_user_groups(user_id_to_app_id(fb_id))[0]: # Uses 0 because it is an [] of [] by accident

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

# TODO: Update or deprecate this function
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
        fb_id = utils.verify_token(access_token, fb_key)
        if fb_id is None:
            response['status'] = 'error'
            response['error-msg'] = 'Invalid FB ID!'
            return jsonify(response)

        # Check if the user is in the group
        print(itils.get_user_groups(utils.user_id_to_app_id(fb_id)))
        if group_id not in utils.get_user_groups(utils.user_id_to_app_id(fb_id))[0]:
            response['status'] = 'error'
            response['error-msg'] = 'You are not in the group!'
            return jsonify(response)

        # Limit the number of returned messages per queue
        max_messages = min(max_messages, 100)
        db = couch['msg_{}'.format(group_id)]

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

@application.route('/inbox/feed/', methods=['GET'])
def get_feed():
    try:
        # TODO: add authentication check
        group_id = request.args.get('group_id')
        access_token = request.args.get('access_token', None)
        max_messages = int(request.args.get('max_messages', 0))
        start_msg = request.args.get('start', None)

        # Limit the number of returned messages per queue
        max_messages = min(max_messages, 100)
        db = couch['msg_{}'.format(group_id)]

        response = {'status': 'ok'}    

        # Check if token is valid
        fb_id = utils.verify_token(access_token, fb_key)
        if fb_id is None:
            response['status'] = 'error'
            response['error-msg'] = 'Invalid FB ID!'
            return jsonify(response)

        # Check if the user is in the group
        groups = utils.get_user_groups(fb_id)
        print(groups)

        if group_id not in utils.get_user_groups(utils.user_id_to_app_id(fb_id))[0]:
            response['status'] = 'error'
            response['error-msg'] = 'You are not in the group!'
            return jsonify(response)

        # If no starting message is provided, start with the newest
        # TODO: Add check for specific group
        gen = None
        if start_msg is not None:
            msg_to_start = db[start_msg]
            print('Search for:', msg_to_start)
            msg_to_start = msg_to_start['running_score'] + msg_to_start['score']
            gen = db.iterview('chats/getRankedMsgs', 20, limit=max_messages, startkey=msg_to_start, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        else:
            gen = db.iterview('chats/getRankedMsgs', 20, limit=max_messages, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        msgs = [m.value for m in gen if fb_id not in m.value['dislikes']]
        print(msgs)
        msgs = id_to_name(msgs)

        return jsonify(msgs)

    except Exception as e:
        raise e


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
        db = couch['msg_{}'.format(group_id)]

        response = {'status': 'ok'}    

        # Check if token is valid
        fb_id = utils.verify_token(access_token, fb_key)
        if fb_id is None:
            response['status'] = 'error'
            response['error-msg'] = 'Invalid FB ID!'
            return jsonify(response)

        # Check if the user is in the group
        groups = utils.get_user_groups(fb_id)
        print(groups)

        if group_id not in utils.get_user_groups(utils.user_id_to_app_id(fb_id))[0]:
            response['status'] = 'error'
            response['error-msg'] = 'You are not in the group!'
            return jsonify(response)

        # If no starting message is provided, start with the newest
        # TODO: Add check for specific group
        gen = None
        msgs = None
        if max_messages > 0:
            if start_msg is not None:
                gen = db.iterview('chats/getGroupMsgs', 20, limit=max_messages, startkey=start_msg, descending=True)
            else:
                gen = db.iterview('chats/getGroupMsgs', 20, limit=max_messages, descending=True)

            msgs = [m.value for m in gen if fb_id not in m.value['dislikes']]

        # User wants the messages in reverse order
        elif max_messages < 0:
            if start_msg is not None:
                gen = db.iterview('chats/getGroupMsgs', 20, endkey=start_msg, descending=True)
            else:
                response['status'] = 'error'
                response['error-msg'] = 'Must specify start for reverse search!'
                return jsonify(response)

            
            msgs = [m.value for m in gen if fb_id not in m.value['dislikes']]
            msgs.reverse()
            msgs = msgs[:min(-max_messages, len(msgs) )]

        print(msgs)
        msgs = id_to_name(msgs)

        return jsonify(msgs)

    except Exception as e:
        raise e

if __name__ == "__main__":
    application.debug = True
    application.run(host='0.0.0.0', port=5000)
