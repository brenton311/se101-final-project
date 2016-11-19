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

__author__ = "Raghav Sood"
__copyright__ = "Copyright 2014"
__credits__ = ["Raghav Sood"]
__license__ = "CC"
__version__ = "1.0"
__maintainer__ = "Raghav Sood"
__email__ = "raghavsood@appaholics.in"
__status__ = "Production"


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
"cookie": "datr=ur7xVysz10aPNLg-GNF-pzQW; locale=en_US; sb=9qEqWAyh9cOPQoyCW7z2iAZ8; c_user=100014262394138; xs=25%3ACCs_fSuEIqKUTQ%3A2%3A1479399287%3A-1; fr=0YEDN2KhNtV8svjFc.AWX-2U4A7kmTXp91GnBmCw6qUJc.BX8b66.mw.AAA.0.0.BYLdd3.AWXtwUuS; csm=2; s=Aa7ow7DXSoUvaYoU.BYLdd3; pl=n; lu=ggUtnl5ELav9__4E9jywIqBg; act=1479404555532%2F3; presence=EDvF3EtimeF1479405141EuserFA21B14262394138A2EstateFDsb2F1479404966745Et2F_5bDiFA2user_3a1B02929652692A2CAcDiFA2thread_3a1150546131643551A2EsiFA21150546131643551A2ErF1CAcDiFA2thread_3a1065671046884259A2EsiFA21065671046884259A2ErF1C_5dElm2FA2root_3a6205072458029973168A2Euct2F1479404570634EtrFnullEtwF414389175EatF1479405140924G479405141571CEchFDp_5f1B14262394138F123CC; p=-2", 
"pragma": "no-cache", 
"user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.122 Safari/537.36", 
"content-type": "application/x-www-form-urlencoded", 
"accept": "*/*", 
"cache-control": "no-cache", 
"referer": "https://www.facebook.com/messages/zuck"}

def download_latest_msgs(num_msgs, chat_id):
	data_text = {"messages[thread_fbids][" + str(chat_id) + "][offset]": str(0), 
	"messages[thread_fbids][" + str(chat_id) + "][limit]": str(num_msgs), 
	"client": "web_messenger", 
	"__user": "100014262394138", 
	"__a": "1", 
	"__dyn": "7AmajEzUGByAZ112u6W85k2mq2WiWF7By8Z9LFwxBxCbzEeAq2i5U4e2CEaUgxebkwy3eF8W49XDG4XzFE8oiyUpwGDwPKq4GCzEkxu9AzUO5onwnoCium8yUgx66EK3Ou49LZ1HgkBx-2jAyEhzE-49oG9z8", 
	"__req": "B", 
	"fb_dtsg": "AQHTWLczThUO:AQGbQS3R6kpb:AQFl87VgqJfM", 
	"ttstamp": "265817198818351825410711298586581701085655861031137410277", 
	"__rev": "2689812"}
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

def get_user_name(user_id, token):
	graph_url = "https://graph.facebook.com/"
	r = requests.get("{}{}".format(graph_url, user_id), params={"access_token": app_key})
	# print(r.text)
	
	json_data = json.loads(r.text)

	return json_data["name"]

def get_newest_msg(msgs_json):
	base_newest = 0
	for msg in msgs_json['payload']['actions']:
		timestamp = int(msg['timestamp'])
		if timestamp > base_newest:
			base_newest = timestamp

	return base_newest

def get_messages_since(new_json, since_time):
	new_msgs = []

	for msg in new_json['payload']['actions']:
		timestamp = int(msg['timestamp'])
		if timestamp > since_time:
			new_msgs.append(msg)
			
	return new_msgs

def save_msgs(db, msgs):
	for msg in msgs:
		# print(msg)
		# Take the data we need out of the json
		zip_msg = {}
		zip_msg['author_id'] = msg.get('author', '').split(':')[1]
		zip_msg['timestamp'] = msg.get('timestamp', '')
		zip_msg['attachments'] = msg.get('attachments', '')
		zip_msg['group_id'] = msg.get('thread_id', '')
		zip_msg['text'] = msg.get('body', '')

		db.save(zip_msg)

if __name__ == '__main__':
	# SEXX'I = 1150546131643551
	# Smartest People in Canada = 1127396163964738
	group_id = '1150546131643551'

	db_msgs = couch['messages']

	max_msgs = 10
	data = json.loads(download_latest_msgs(200, group_id))
	# print(data)
	save_msgs(db_msgs, get_messages_since(data, 0))
	newest_time = get_newest_msg(data)


	print('Waiting for messages...')
	while True:
		new_data = json.loads(download_latest_msgs(max_msgs, group_id))
		# new_msgs = merge_messages(data, new_data)

		new_msgs = get_messages_since(new_data, newest_time)
		for x in new_msgs:
			try:
				fb_id = x["author"].split(":")[1]
				# print(fb_id)
				user_name = get_user_name(fb_id, app_key)
				# print(user_name)
				# user_name = get_user_info(x["author"].split(":")[1], app_key)
				print(u'[{} ({} @ {})]: {}'.format(user_name, x['timestamp_absolute'], x['timestamp_datetime'], x['body']))
			except Exception:
				print("Error")

		save_msgs(db_msgs, new_msgs)

		newest_time = get_newest_msg(new_data)
		time.sleep(1)
