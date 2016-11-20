from flask import Flask, request
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

######################################################################
# VIEWS
######################################################################
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
        view_url = 'http://138.197.131.3:5984/messages/_design/testView/_view/getGroupMsgs'
        view_json = json.loads(download_text(view_url))
        author_table = {}


        for row in view_json['rows']:
            if row['key'] == group_id:
                # group_msgs += 
                # author_id = db[row['id']]['author_id']
                author_id = row['value']['author']
                if author_id not in author_table:
                    author_table[author_id] = get_user_name(author_id, fb_key)
                new_msg = '[{}]: {}<br>'.format(author_table[author_id], row['value']['text'])
                
                group_msgs += new_msg

        return str(group_msgs)

    except Exception as e:
        raise e
        # return 'Error: {}'.format(e)


if __name__ == "__main__":
    application.debug = True
    application.run(host='0.0.0.0')
