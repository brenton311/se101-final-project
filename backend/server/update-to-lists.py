import couchdb

couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')

if __name__ == '__main__':
    db = couch['messages']
    for row in db:
        msg = db[row]

        # Ignore views
        if 'text' not in msg: 
            continue

        msg['likes'] = []
        msg['dislikes'] = []
        msg['bookmarks'] = []
        del msg['unlikes']
        db.save(msg)