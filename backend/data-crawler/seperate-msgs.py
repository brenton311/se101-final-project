import couchdb
import sys

couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')

if __name__ == '__main__':
    group_id = sys.argv[1]
    db_main = couch['messages']
    db_new = couch['msg_{}'.format(group_id)]
    # db_new = couch.create('msg_{}'.format(group_id))
    
    print('Extracting messages from messages to {}'.format(db_new.name))

    gen = db_new.iterview('chats/getGroupMsgs', 100, descending=True)# 'startkey="41b40f7d7e0037e9f16195cf0a07422a"&descending=true&limit=10')
    msgs = [db_new[m.key] for m in gen]
    # print(msgs, len(msgs))
    # print(msgs[0].values())
    # print(msgs[0].copy())

    for m in msgs:
        if m['group_id'] == group_id:
            new_msg = m.copy()

            # Remove the db specific values so the new db will order
            # it in the order they come in
            new_msg.pop('_id')
            new_msg.pop('_rev')

            # print(new_msg)
            # db_new.save(new_msg)
        else:
            print(m)