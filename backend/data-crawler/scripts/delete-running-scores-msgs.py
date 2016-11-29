import couchdb
couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')
db = couch['msg_1150546131643551']
# gen = db.iterview('chats/getGroupMsgs', 100, descending=True)
# msgs = [m for m in gen]
# print(msgs)

for m in db:
    print(db[m])
    # if 'running_score' in m.value:
        # print(m)