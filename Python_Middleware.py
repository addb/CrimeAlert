import ast
import calendar
import csv
import datetime
import itertools
import json
import pymongo
import requests
import time
import traceback
import sys
from web import *
from pprint import pprint
from geopy.distance import vincenty
from bson.son import SON
from bson.json_util import dumps
from bson import BSON
from flask import Flask, request, render_template

app = Flask(__name__)
mongolab_uri = "mongodb://crime_UCI:nocrime123@ds025439.mlab.com:25439/crime_alert_db"
middleware_uri = "http://localhost:5000"
crime_type = ["User Distress", "Abduction", "Burglary", "Murder", "Shooting"]
min_dist = 50
min_time = 300
meta = dict()
for c in crime_type:
	meta[c] = dict()

def connect_db(mongolab_uri):
	try:
		connect = pymongo.MongoClient(mongolab_uri,
	                 connectTimeoutMS=30000,
	                 socketTimeoutMS=None,
	                 socketKeepAlive=True)
	except pymongo.errors.ConnectionFailure, e:
	    print "Connection to MongoDB failed: %s" % e 
	handle = connect.get_default_database()
	handle.User.create_index([('Location', pymongo.GEO2D)])
	return handle

def retrieve_collection(handle, coll_name):
	handle_coll = handle[coll_name]
	for d in handle_coll.find():
		print d

def insert_data(handle, coll_name, data):
	handle_coll = handle[coll_name]
	handle_coll.insert_one(data)

def insert_aggregate_db(ts1, ts2):
	try:
		tsp1 = time.strftime('%m/%d/%Y %H:%M:%S', time.localtime(ts1))
		tsp2 = time.strftime('%m/%d/%Y %H:%M:%S', time.localtime(ts2))
		handle_crime = handle['CrimeEvent']
		handle_spatial = handle['Spatial_Crime']
		try:
			d1 = [x for x in handle_crime.find({"Timestamp": tsp1})][0]
			d2 = [x for x in handle_crime.find({"Timestamp": tsp2})][0]
		except:
			print "Error in insert_aggregate_db"
			return
		d = dict()
		d['Number of users'] = 2
		d['Reporter-1'] = str(d1['Reporter']) + '   ' + d1['Email'] + '   ' + d1['Description'] + \
					   '   ' + str(d1['Location'])
		d['Timestamp-1'] = d1['Timestamp']
		d['Reporter-2'] = str(d2['Reporter']) + '   ' + d2['Email'] + '   ' + d2['Description'] + \
					   '   ' + str(d2['Location'])
		d['Timestamp-2'] = d2['Timestamp']
		d['Type'] = d1['Type']
		d['Priority'] = "High"
		print "Inserting in Spatial DB: " + str(d)
		insert_data(handle, "Spatial_Crime", d)
	except:
		print traceback.print_stack()

def find_users_location2(lat, lon, dist):
	coll_name = "User"
	handle_coll = handle[coll_name]
	coord1 = [lat, lon]
	op = []
	for d in handle_coll.find():
		l1 = d['Latitude']
		l2 = d['Longitude']
		coord2 = [l1, l2]
		if vincenty(coord1, coord2) < dist:
			op.append(d['GCMToken'])
	return op

def find_users_location(lat, lon, dist):
	coll_name = "User"
	handle_coll = handle[coll_name]
	query = {"Location": SON([("$near", [lat, lon]), ("$maxDistance", min_dist)])}
	op = []
	for d in handle_coll.find(query):
		print d
		tok = d['GCMToken']
		if tok not in op:
			op.append(tok)
	return op
	
def check_spatial_meta(loc, ts, crime):
	global meta
	loc = tuple(loc)
	flag = False
	meta_sub = meta[crime]
	if meta[crime]:
		for key in meta[crime].keys():
			if vincenty(key, loc) < min_dist:
				flag = True
				if abs(ts - meta[crime][key]) < min_time:
					 return (ts, meta[crime][key])
	if not flag:
		meta[crime][loc] = ts
	return None
	print traceback.print_stack()

def clean_spatial_meta():
	curr_time = time.time()
	for key in meta.keys():
		for k in key:
			if meta[key][k] - curr_time > 900:
				meta[key].pop(key, None)

def parse_location(sr):
	sr = sr[0]
	return sr['Latitude'], sr['Longitude']

def parse_time(t):
	ts = time.strptime(t, '%m/%d/%Y %H:%M:%S')
	epoch_time = int(time.mktime(ts))
	return epoch_time

def check_history(event):
	l1, l2 = parse_location(event['Location'])
	loc = [l1, l2]
	time = parse_time(event['Timestamp'])
	event_type = event['Type']
	if event_type is "User Distress":
		return None
	hist = check_spatial_meta(loc, time, event_type)
	if hist:
		print hist
		insert_aggregate_db(hist[0], hist[1])
	else:
		return None

def receive_crime_event(sr):
	crime_event = json.loads(sr)
	print type(crime_event)
	check_history(crime_event)
	lat, lon = parse_location(crime_event['Location'])
	users = find_users_location2(lat, lon, min_dist)
	return users

def send_users_list(json_op):
	r = requests.post(middleware_uri, json=json_op)

@app.route('/', methods=['GET', 'POST'])
def get_results():
	print "POST data: "
	pprint(request)
	pprint(request.data)
	try:
		if request.method == 'POST':
			op = receive_crime_event(request.data)
			op = ','.join(set(op))
			print "Output: " + op
			print "\n\n"
			return op
		else:
			print "Not a POST request"
	except:
		print traceback.print_stack()
	return "200 OK - End of session.."

if __name__ == '__main__':
	handle = connect_db(mongolab_uri)
	app.run(debug=True, host='0.0.0.0')
