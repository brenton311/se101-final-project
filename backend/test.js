var firebase = require("firebase");

// Initialize the app with a service account, granting admin privileges
firebase.initializeApp({
  serviceAccount: "./Pronto-fcdd5a2ef19a.json",
  databaseURL: "https://pronto-90f2d.firebaseio.com/"
});

// As an admin, the app has access to read and write all data, regardless of Security Rules
var db = firebase.database();
/*var ref = db.ref("restricted_access/secret_document");
ref.once("value", function(snapshot) {
  console.log(snapshot.val());
});*/


//The data for our app will be stored at this database reference:
var ref = db.ref("server/saving-data/fireblog");
//note: refernces in path will be created if do not exist in database

/*First, we'll create a database reference to our user data. Then we'll use set() / setValue() 
to save a user object to the database with the user's username, full name, and birthday. 
We can pass the function set a string, number, boolean, null, array or any JSON object. 
Passing null will remove the data at the specified location. In this case we'll pass it an object:*/
var usersRef = ref.child("users");
usersRef.set({
  alanisawesome: {
    date_of_birth: "June 23, 1912",
    full_name: "Alan Turing"
  },
  gracehop: {
    date_of_birth: "December 9, 1906",
    full_name: "Grace Hopper"
  }
});
/*When a JSON object is saved to the database, the object properties are automatically 
mapped to database child locations in a nested fashion. Now if we navigate to the URL 
https://docs-examples.firebaseio.com/server/saving-data/fireblog/users/alanisawesome/full_name, 
we'll see the value "Alan Turing". You can also save data directly to a child location:*/
usersRef.child("alanisawesome").set({
  date_of_birth: "June 23, 1912",
  full_name: "Alan Turing"
});
usersRef.child("gracehop").set({
  date_of_birth: "December 9, 1906",
  full_name: "Grace Hopper"
});
/*The first example will only trigger one event on clients that are watching the data, whereas 
the second example will trigger two. It is important to note that if data already existed at usersRef, 
the first approach would overwrite it, but the second method would only modify the value of each 
separate child node while leaving other children of usersRef unchanged.*/

/*If you want to write to multiple children of a database location at the same time without 
overwriting other child nodes, you can use the update method as shown below:*/
var hopperRef = usersRef.child("gracehop");
hopperRef.update({
  "nickname": "Amazing Grace"
});
//note nickname reference will be created if does not exist
/*This will update Grace's data to include her nickname. 
If we had used set here instead of update, it would have deleted both 
full_name and date_of_birth from our hopperRef.*/

//The Firebase Realtime Database also supports multi-path updates
usersRef.update({
  "alanisawesome/nickname": "Alan The Machine",
  "gracehop/nickname": "Amazing Grace"
});

/*
Note that trying to update objects by writing objects with the paths included 
will result in different behavior. Let's take a look at what happens if we instead 
try to update Grace and Alan this way:

usersRef.update({
  "alanisawesome": {
    "nickname": "Alan The Machine"
  },
  "gracehop": {
    "nickname": "Amazing Grace"
  }
});

This results in different behavior, namely overwriting the entire /users node */
