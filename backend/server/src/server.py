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
    return fb_id is not None

######################################################################
# VIEWS
######################################################################
@application.route("/")
def hello():
    return "<h1 style='color:blue'>Hello There!</h1>"

@application.route('/login', methods=['POST'])
def login():
    access_token = request.form.get('access_token', None)
    if access_token is None:
        return 'Error'

    return str(verify_token(access_token, fb_key))

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
            gen = db.iterview('chats/getMsgsRanks', max_messages, startkey=start_msg, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        else:
            gen = db.iterview('chats/getMsgsRanks', max_messages, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')        
        msgs = [m.value for m in gen]
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
        max_messages = min(max_messages, 30)
        db = couch['messages']

        # If no starting message is provided, start with the newest
        gen = None
        if start_msg is not None:
            gen = db.iterview('chats/getGroupMsgs', max_messages, limit=max_messages, startkey=start_msg, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        else:
            gen = db.iterview('chats/getGroupMsgs', max_messages, limit=max_messages, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
        msgs = [m.value for m in gen]
    
        return jsonify(msgs)

    except Exception as e:
        raise e

if __name__ == "__main__":
    application.debug = True
    application.run(host='0.0.0.0')
