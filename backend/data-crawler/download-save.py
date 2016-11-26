import sys
from download import download_latest_msgs, extract_msgs, save_msgs, couch

if __name__ == '__main__':
    print('Saving messages!')
    group_id = sys.argv[1]
    num_messages = int(sys.argv[2])
    data = download_latest_msgs(num_messages, group_id)
    print(data)
    msgs = extract_msgs(data, group_id)
    save_msgs(couch['msg_{}'.format(group_id)], msgs)