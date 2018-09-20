function (doc) {
	emit(doc.timestamp, {
		'author_id': doc.author_id, 
		'timestamp': doc.timestamp, 
		'text': doc.text, 
		'msg_id': doc._id, 
		'likes': doc.likes, 
		'dislikes' :doc.dislikes,
		'attachments' :doc.attachments,
		'bookmarks': doc.bookmarks
	});
}