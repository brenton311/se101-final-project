import couchdb

couch = couchdb.Server()

if __name__ == '__main__':
    db = couch['msg_1150546131643551']
    for row in db:
        msg = db[row]

        # Ignore views
        if 'text' not in msg: 
            continue

        msg['likes'] = []
        msg['dislikes'] = []
        msg['bookmarks'] = []
        if 'unlikes' in msg:
            del msg['unlikes']
        db.save(msg)