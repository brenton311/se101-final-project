import requests

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