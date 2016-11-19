from flask import Flask, request
import requests
import couchdb
import json

application = Flask(__name__)
couch = couchdb.Server('http://dev:pronto@0.0.0.0:5984')
fb_key = "1117295381688482|EwDDv3rzCr5C-9QwpSm6qkE-7L8"

def get_user_name(user_id, token):
	graph_url = "https://graph.facebook.com/"
	r = requests.get("{}{}".format(graph_url, user_id), params={"access_token": token})
	json_data = json.loads(r.text)

	return json_data.get('name', 'Name not found!')

@application.route("/")
def hello():
    return "<h1 style='color:blue'>Hello There!</h1>"

@application.route("/inbox/", methods=['GET'])
def get_msgs():
    # Arguments:
    #   group_id
    #   user_token

    try:
        group_id = request.args.get('group_id')
        access_token = request.args.get('access_token', None)

        db = couch['messages']
        group_msgs = ''
        for msg in db:
            # print(msg)
            # msg_dict = json.loads(msg)
            # print(db[msg])
            if db[msg]['group_id'] == group_id:
                author_id = db[msg]['author_id']
                author_name = get_user_name(author_id, fb_key)
                group_msgs += '[{}]: {}<br>'.format(author_name, db[msg]['text'])

        return str(group_msgs)

    except Exception as e:
        raise e
        # return 'Error: {}'.format(e)


if __name__ == "__main__":
    application.debug = True
    application.run(host='0.0.0.0')
