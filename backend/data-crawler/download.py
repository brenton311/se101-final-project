import urllib2
import urllib
import gzip
import os
import json
import sys
import time
import StringIO
import requests
import time
import couchdb
import math

couch = couchdb.Server('http://dev:pronto@0.0.0.0:5984')

app_key = "1117295381688482|EwDDv3rzCr5C-9QwpSm6qkE-7L8"

error_timeout = 30 # Change this to alter error timeout (seconds)
general_timeout = 1 # Change this to alter waiting time afetr every request (seconds)
messages = []
offset = 0 #int(sys.argv[3]) if len(sys.argv) >= 4 else int("0")
messages_data = "lolno"
end_mark = "\"payload\":{\"end_of_history\""
headers = {"origin": "https://www.facebook.com", 
"accept-encoding": "gzip,deflate", 
"accept-language": "en-US,en;q=0.8", 
"cookie": "datr=ur7xVysz10aPNLg-GNF-pzQW; sb=9qEqWAyh9cOPQoyCW7z2iAZ8; pl=n; lu=ggUtnl5ELav9__4E9jywIqBg; c_user=100014262394138; xs=25%3ACCs_fSuEIqKUTQ%3A2%3A1479399287%3A10124; fr=0YEDN2KhNtV8svjFc.AWUs_-VgixY7AzfM7KkwssOP0ak.BX8b66.mw.AAA.0.0.BYN2a_.AWVIPusW; csm=2; s=Aa7ow7DXSoUvaYoU.BYLdd3; p=-2; act=1480029271860%2F4; presence=EDvF3EtimeF1480029307EuserFA21B14262394138A2EstateFDsb2F1480028616862Et2F_5bDiFA2thread_3a1150546131643551A2EsiFA21150546131643551A2ErF1C_5dElm2FnullEuct2F1480020178394EtrFA2loadA2EtwF1335759719EatF1480029306319EwmlFDfolderFA2inboxA2Ethread_5fidFA2thread_3a1150546131643551A2CG480029307396CEchFDp_5f1B14262394138F99CC", 
"pragma": "no-cache", 
"user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.122 Safari/537.36", 
"content-type": "application/x-www-form-urlencoded", 
"accept": "*/*", 
"cache-control": "no-cache", 
"referer": "https://www.facebook.com/messages/zuck"}

def fetch_msgs(chat_id, nums):
	data_text = {"messages[thread_fbids][" + str(chat_id) + "][offset]": str(0), 
	"messages[thread_fbids][" + str(chat_id) + "][limit]": str(nums), 
	"client": "web_messenger", 
	"__user": "100014262394138", 
	"__a": "1", 
	"__dyn": "7AgNeCjyGgNfgggDxKA5Q2mq2WiWKum8zXrWo461lDwAxu13wm8gxZoK3eE99XyEjKewExW6oaFUcXCxmqewIyo8obo5S9ADxG48hwCwYDx2q-EsgkBx-2qi78WfwHz8C", 
	"__req": "3", 
	"fb_dtsg": "AQEkIFWxOpdc:AQGUzTLprJOF", 
	"ttstamp": "2658169107737087120791121009958658171851228476112114747970", 
	"__rev": "2693755"}
	data = urllib.urlencode(data_text)
	url = "https://www.facebook.com/ajax/mercury/thread_info.php"
	
	# print("Retrieving messages " + str(offset) + "-" + str(num_msgs+offset) + " for conversation ID " + str(chat_id))
	req = urllib2.Request(url, data, headers)
	response = urllib2.urlopen(req)
	compressed = StringIO.StringIO(response.read())
	decompressedFile = gzip.GzipFile(fileobj=compressed)
	
	messages_data = decompressedFile.read()
	messages_data = messages_data[9:]

	return messages_data


def download_latest_msgs(num_msgs, chat_id):
	batch_size = 30

	msgs = None
	for i in range(0, int(math.ceil(num_msgs / 30.0))):
		new_data = json.loads(fetch_msgs(chat_id, batch_size))
		if msgs is None:
			msgs = new_data
		else:
			msgs['payload']['actions'].append(new_data['payload']['actions'])

	return json.dumps(msgs)

def get_user_name(user_id, token):
	graph_url = "https://graph.facebook.com/"
	r = requests.get("{}{}".format(graph_url, user_id), params={"access_token": token})
	# print(r.text)
	
	json_data = json.loads(r.text)

	return json_data.get('name', 'Name not found!')

def get_newest_msg(msgs):
	base_newest = 0
	# print(msgs)
	for msg in msgs:
		timestamp = int(msg['timestamp'])
		if timestamp > base_newest:
			base_newest = timestamp

	return base_newest

def find_oldest_msg(msgs):
	base_newest = float('inf')
	# print(msgs)
	for msg in msgs:
		timestamp = int(msg['timestamp'])
		if timestamp < base_newest:
			base_newest = timestamp

	return base_newest

"""
	Extracts the useful fields from the msg dict
"""
def compress_msg(msg, group_id):
	zip_msg = {}

	# Facebook provided data
	zip_msg['author_id'] = msg.get('author', '').split(':')
	if len(zip_msg.get('author_id')) > 1:
		zip_msg['author_id'] = zip_msg.get('author_id')[1]
	else:
		zip_msg['author_id'] = 'Error'

	zip_msg['timestamp'] = msg.get('timestamp', '')
	zip_msg['attachments'] = msg.get('attachments', '')
	# zip_msg['group_id'] = msg.get('thread_id', '')
	zip_msg['group_id'] = group_id
	zip_msg['text'] = msg.get('body', '')

	# Custom data
	zip_msg['score'] = 0.0
	zip_msg['likes'] = []
	# zip_msg['unlikes'] = []
	zip_msg['dislikes'] = []
	zip_msg['bookmarks'] = []

	return zip_msg

"""
	Takes in a FB json of messages, compress them, returns
	messages in a list
"""
def extract_msgs(json_text, group_id):
	msgs_json = json.loads(json_text)
	msgs = []

	for msg in msgs_json['payload']['actions']:
		msgs.append(compress_msg(msg, group_id))

	return msgs

def get_messages_since(msgs, since_time):
	new_msgs = []

	for msg in msgs:
		timestamp = int(msg['timestamp'])
		if timestamp > since_time:
			new_msgs.append(msg)
			
	return new_msgs

def save_msgs(db, msgs):
	for msg in msgs:
		db.save(msg)

def find_newest_msg(db):
	base_newest = 0
	for msg_doc in db:
		msg = db[msg_doc]
		# print(msg)
		timestamp = int(msg.get('timestamp', 0))
		if timestamp > base_newest:
			base_newest = timestamp

	return base_newest


if __name__ == '__main__':
	# SEXX'I = 1150546131643551
	# Smartest People in Canada = 1127396163964738
	# Pronto, George, Brenton = 1065671046884259

	group_id = '1150546131643551'
	# group_id = '1127396163964738'
	# group_id = '1065671046884259'

	db_msgs = couch['messages']

	max_msgs = 30
	newest_time = find_newest_msg(db_msgs)

	print('Starting...')
	while True:
		try:
			oldest_updated = float('inf')
			new_msgs = []
			while oldest_updated > newest_time:
				print('Getting new messages...')
				new_data = download_latest_msgs(max_msgs, group_id)
				# print(new_data)

				new_msgs = extract_msgs(new_data, group_id)
				new_msgs.extend(get_messages_since(new_msgs, newest_time))
				# print(new_msgs)

				if len(new_msgs) > 0:
					oldest_updated = find_oldest_msg(new_msgs)

			# Ignore the messages already downloaded
			new_msgs = get_messages_since(new_msgs, newest_time)

			for x in new_msgs:
				try:
					user_name = get_user_name(x['author_id'], app_key)
					print(u'[{}]: {}'.format(user_name, x['text']))
				except Exception as e:
					print('Error: {}'.format(e))

			# If any new messages come in, update the newest timestamp
			if len(new_msgs) > 0:
				newest_time = get_newest_msg(new_msgs)

			save_msgs(db_msgs, new_msgs)

			time.sleep(1)
		except Exception as e:
			print('Error: ' + str(e))