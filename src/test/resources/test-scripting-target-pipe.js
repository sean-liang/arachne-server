{
    initialize: function() { console.log('init'); },
    destroy: function() { console.log('destroy'); },
    proceed: function(stream, feedback, context) {
        if(!stream) {
            throw new DropFeedbackException();
        }
        return stream.map(i => i * 2);
    },
}