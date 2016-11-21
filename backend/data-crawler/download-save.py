import sys
from download import download_latest_msgs, extract_msgs, save_msgs, couch

if __name__ == '__main__':
    print('Saving messages!')
    group_id = sys.argv[1]
    num_messages = sys.argv[2]
    data = download_latest_msgs(num_messages, group_id)
    msgs = extract_msgs(data, group_id)
    save_msgs(couch['messages'], msgs)