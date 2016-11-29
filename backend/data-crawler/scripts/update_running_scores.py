"""
    The purpose of this script is to periodically update the running score
    of all messages in the database
"""

import time
import math
import couchdb
couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')

def compute_rank(num_likes, num_dislikes, num_bookmarks, time_delta):
    return num_likes - 3 * num_dislikes + 10 * num_bookmarks - 0.1 * (time_delta)
 
if __name__ == '__main__':
    group_id = '1150546131643551'
    db = couch['msg_{}'.format(group_id)]
    time_now = int(time.time())

    while True:
        print('Updating...')

        # Retreive all messages
        gen = db.iterview('chats/getRankedMsgs', 100, descending=True)
        msgs = [m for m in gen]
        for row in msgs:
            msg = db[row.id]

            # print('Before:', msg)
            # m = row.value

            # FB timestamps are in microseconds
            time_delta = (time_now - int(msg['timestamp']) / 1000.0) / 3600
            num_likes = len(msg['likes'])
            num_dislikes = len(msg['dislikes'])
            num_bookmarks = len(msg['bookmarks'])

            msg['running_score'] = compute_rank(num_likes, num_dislikes, num_bookmarks, time_delta)
            # print('After:', msg)
            db.save(msg)

            # db.save(row)
        
        time.sleep(10)