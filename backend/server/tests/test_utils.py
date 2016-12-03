import requests
import sys

sys.path.insert(0, '../')
from src import utils

url = 'http://www.prontoai.com:5000'
access_token = None

def test_server_up():
    r = requests.get(url)
    assert r.status_code == 200
    assert r.text == "<h1 style='color:blue'>Hello There!</h1>"

def test_download_text():
    r = requests.get(url + '/test-file/')
    assert r.status_code == 200
    assert r.text == "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

# What we have to test on the server:
#   get Facebook username
#   download text
#   verify facebook token
#   get_user_groups
#   id_to_name
# 
# 

def test_get_user_name():
    pass

# TODO: remove api_token field
def test_verify_token():
    pass

def test_get_user_groups():
    pass

def test_id_to_name():
    pass

# Find the user_id associated with an app id
def test_user_id_to_app_id():
    pass