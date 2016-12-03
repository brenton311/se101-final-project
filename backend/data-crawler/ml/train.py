import rankmsgs as rm
import linearreg as linreg
import numpy as np
import vocab
import couchdb

couch = couchdb.Server('http://dev:pronto@prontoai.com:5984')

def compute_rank(num_likes, num_dislikes, num_bookmarks, time_delta):
    return num_likes - 3 * num_dislikes + 10 * num_bookmarks - 0.1 * (time_delta)

if __name__ == '__main__':
    msg_db = couch['msg_1150546131643551']
    msgs = rm.load_messages(msg_db.name)
    # print(msgs)
    features = []
    print(features)
    for m in msgs:
        tokens = rm.tokenize_msg(m, vocab.vocab)['words_freq']
        word_freq = list(tokens.values())

        # print(word_freq)
        features.append(word_freq)
        # word_vector = rm.load_dict_to_vector(tokens)
        # print(word_vector)
        # features = features.append(word_vector)
        # features = np.vstack([features, word_vector])
        # features.append(word_vector)


    print(features)
        # m_features = [{k: m[k]} for k in m]
        # print(m_features)