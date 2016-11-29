from ml import linearreg
from ml import vocab

import numpy as np
import couchdb
couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')

"""
    Extracts all the info from each message
    that is useful to our Machine Learning 
    algorithm
"""
def extract_msg(msg, id):
    relevant_data = {}
    relevant_data['_id'] = id
    relevant_data['text'] = msg['text']
    relevant_data['likes'] = msg['likes']
    relevant_data['dislikes'] = msg['dislikes']
    relevant_data['bookmarks'] = msg['bookmarks']

    return relevant_data

"""
    Loads messages from the 'db_name' databse
    into memory. Loads 'limit' messages if limit != 0,
    loads all messages otherwise
"""
def load_messages(db_name, limit=0):
    db = couch[db_name]
    gen = None
    if limit > 0:
        gen = db.iterview('chats/getGroupMsgs', 100, limit=limit)
    else:
        gen = db.iterview('chats/getGroupMsgs', 100)

    msgs = [ extract_msg(m.value, m.key) for m in gen]
    return msgs

"""
    Count the occurance of each element of vocab in each msg
    Returns a list of dictionarys with the results for each msg
"""
# def tokenize_msgs(msgs, vocab):
#     tokenized_msgs = []
#     for m in msgs:
#         text = m['text']
#         words_freq = {word: 0 for word in vocab}
#         for word in vocab:
#             freq = text.lower().count(word.lower())
#             if freq > 0:
#                 words_freq[word] = freq

#         tokenized_msgs.append( {'_id': m['_id'], 'words_freq': words_freq} )

#     return tokenized_msgs

def tokenize_msg(msg, vocab):
    text = msg['text']
    words_freq = {word: 0 for word in vocab}
    for word in vocab:
        freq = text.lower().count(word.lower())
        if freq > 0:
            words_freq[word] = freq

    tokenized_msg = {'words_freq': words_freq}
    return tokenized_msg

def load_model_settings(db_name, group_id):
    db = couch[db_name]
    for doc in db:
        if db[doc]['group_id'] == group_id:
            return db[doc]
    

def save_model_settings(db_name, setttings):
    db = couch[db_name]
    db.save(settings)
    
def compute_rank(word_vector, weights_vector):
    # print('Computing...')
    # print(word_vector.ravel())
    # print(weights_vector.ravel())
    return np.vdot(weights_vector.ravel(), word_vector.ravel()) #+ weights[0])

def load_dict_to_vector(to_convert):
    weights_list = list(to_convert.values())
    weights = np.array(weights_list).reshape(len(weights_list), 1)
    return weights

def save_rank(msg_id, db, rank):
    msg = db[msg_id]
    msg['score'] = rank
    db.save(msg)

if __name__ == '__main__':
    msg_db = couch['msg_1150546131643551']
    msgs = load_messages(msg_db.name)
    settings = load_model_settings('ml_data', '1150546131643551')

    weights = load_dict_to_vector(settings['weights'])

    for m in msgs:
        tokenized = tokenize_msg(m, vocab.vocab)['words_freq']
        word_vector = load_dict_to_vector(tokenized)
        rank = compute_rank(word_vector, weights)
        save_rank(m['_id'], msg_db, rank)

        # Debug
        if rank > 0:
            print(rank, m)
            for key in tokenized:
                value = tokenized[key]
                if value > 0:
                    print(key, value)