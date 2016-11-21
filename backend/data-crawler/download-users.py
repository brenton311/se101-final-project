import couchdb
import requests
import json

couch = couchdb.Server('http://dev:pronto@0.0.0.0:5984')
# fb_key = "1117295381688482|EwDDv3rzCr5C-9QwpSm6qkE-7L8"
access_token = 'EAACEdEose0cBANyVWEjLyGM2ONoGM74ilgaYPNntZANxpsd4n3a5Y6SbL810mZCuV0aWyd8x1dxqRhT31udrqBf4odV1cbgxQq6aCzrZBM3mMUHeBTMXPO0tfxGxLY6fR3LHwHva0ha6RHo7BQllIZCIy18dbp6ZB8jpjccF89YZCCB8M1fBey'


def download_text(url):
    r = requests.get(url)
    if r.status_code != 200:
        return "Error!"

    return r.text

def does_user_exist(db, user_id):
    for user in db:
        if db[user]['id'] == user_id:
            return db[user]

    return None

if __name__ == '__main__':
    # Smartest People
    # group_id = '1127396163964738'

    # Pronto, George, Brenton = 1065671046884259
    # group_id = '1065671046884259'
    
    # SE'XXI
    group_id = '1150546131643551'

    graph_url = "https://graph.facebook.com/"
    r = requests.get("{}me/inbox".format(graph_url), params={"access_token": access_token})
    json_data = json.loads(r.text)

    # print(json_data)
    group_match = None
    for group in json_data['data']:
        if group['id'] == group_id:
            group_match = group
            break

    db = couch['users']
    for user in group_match['to']['data']:
        existing_user = does_user_exist(db, user['id'])

        # User exists
        if existing_user is not None:
            # print(dir(existing_user)
            # print(existing_user.values())
            print(u'User exists: {}'.format(existing_user))
            if group_id not in existing_user['groups']:
                print('Updated Groups: {}'.format(existing_user['groups']))
                existing_user['groups'].append(group_id)
                db.save(existing_user)
        
        # User does not exist
        else:
            print(u'Creating new user: {}'.format(user))
            user['liked'] = []
            user['disliked'] = []
            user['bookmarks'] = []
            user['groups'] = [group_id]
        
            print(user)
            db.save(user)

    # print(json_data['data'][])