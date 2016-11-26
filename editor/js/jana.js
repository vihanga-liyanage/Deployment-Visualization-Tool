/**
 * Created by wso2123 on 11/25/16.
 */


var showComponentInfo  =  function(state,evt) {
    // console.log(state.style.image)
    // console.log(evt);
    $("#slide_header").text(state.style.image);
    $("#slide_content").text(state.style.image+"Content");

    $('#slide').popup({
        outline: true, // optional
        focusdelay: 400, // optional
        vertical: 'center', //optional
        autoopen:true,

    });
};

var setDetails = function(component, type){

    console.log("setDetails-" + component + '-' + type);
    $('#detail_view_img').attr("src","./images/wso2/" + component + ".png");
    $('#detail_view_title').text(product_details[component].title)
    if(type === 'toolbox'){;
        $('#detail_view_content').html(product_details[component].description);
    }
    else if(type==='graph'){
        $('#detail_view_content').html("Here is some suggestions to connect to "+product_details[component].title +".<br/><br/>")

        var suggestListContent = "<ul class='list-group'>"
        product_suggestions[component].forEach( function (item) {
            suggestListContent+=generateSuggestions(item);
        });
        suggestListContent+="</ul>"
        $('#detail_view_content').append(suggestListContent);
    }
}

var generateSuggestions = function (item) {
    var component = item.component;
    return "<li class='list-group-item'>" +
        "<table><tr><td><img class='suggest_img'  src='"+"./images/wso2/"+component+".png' /></td>" +
        "<td><b> " +product_details[component].title+  "</b>" +
        "<p>" +item.description+
        "</p></td></tr></table> </li>";

}

var getImageNameFromPath = function(path){
    var name = path.split('/')
    name = name[name.length-1]
    return name.substring(0,name.length-4).toLowerCase();
}

setTimeout(function () {
    $('img').on( "click", function (evt) {
        setDetails(getImageNameFromPath(evt.target.src), 'toolbox');
    } );
} ,1000);

// Mouse Move ----------------------------------------------------------------

var addListeners = function (editor) {
    var graph = editor.graph
    graph.addMouseListener(
        {
            currentState: null,
            previousStyle: null,
            mouseDown: function(sender, me)
            {
                if (this.currentState != null)
                {
                    this.dragLeave(me.getEvent(), this.currentState);
                    this.currentState = null;
                    setDetails(me.state.cell.getStyle(), 'graph');
                }
            },
            mouseMove: function(sender, me)
            {
                if (this.currentState != null && me.getState() == this.currentState)
                {
                    return;
                }

                var tmp = graph.view.getState(me.getCell());

                // Ignores everything but vertices
                if (graph.isMouseDown || (tmp != null && !
                        graph.getModel().isVertex(tmp.cell)))
                {
                    tmp = null;
                }

                if (tmp != this.currentState)
                {
                    if (this.currentState != null)
                    {
                        this.dragLeave(me.getEvent(), this.currentState);
                    }

                    this.currentState = tmp;

                    if (this.currentState != null)
                    {
                        this.dragEnter(me.getEvent(), this.currentState);
                    }
                }
            },
            mouseUp: function(sender, me) { },
            dragEnter: function(evt, state)
            {
                if (state != null)
                {
                    this.previousStyle = state.style;
                    state.style = mxUtils.clone(state.style);
                    // updateStyle(state, true);
                    // showDialog();
                    state.shape.apply(state);
                    state.shape.redraw();

                    if (state.text != null)
                    {
                        state.text.apply(state);
                        state.text.redraw();
                    }
                }
            },
            dragLeave: function(evt, state)
            {
                if (state != null)
                {
                    state.style = this.previousStyle;
                    // updateStyle(state, false);
                    state.shape.apply(state);
                    state.shape.redraw();

                    if (state.text != null)
                    {
                        state.text.apply(state);
                        state.text.redraw();
                    }
                }
            }
        });

//--------------------------------- Drop
    mxEvent.addListener(document.getElementById('graph'), 'drop', function(evt)
    {
        console.log("drop");
        if (graph.isEnabled())
        {
            // evt.stopPropagation();
            // evt.preventDefault();
            //
            // // Gets drop location point for vertex
            // var pt = mxUtils.convertPoint(graph.container, mxEvent.getClientX(evt), mxEvent.getClientY(evt));
            // var tr = graph.view.translate;
            // var scale = graph.view.scale;
            // var x = pt.x / scale - tr.x;
            // var y = pt.y / scale - tr.y;
            //
            // // Converts local images to data urls
            // var filesArray = evt.dataTransfer.files;
            //
            // for (var i = 0; i < filesArray.length; i++)
            // {
            //     handleDrop(graph, filesArray[i], x + i * 10, y + i * 10);
            // }
        }
    });
}

