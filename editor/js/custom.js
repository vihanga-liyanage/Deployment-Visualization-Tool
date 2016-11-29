/**
 * Created by wso2123 on 11/25/16.
 */


var showComponentInfo  =  function(state,evt) {
    // console.log(state.style.image)
    // console.log(evt);

    var component = getImageNameFromPath(state.style.image);
    $("#slide_header").text(product_details[component].title);
    $('#slide_img').attr("src","./images/wso2/" + component + ".png");
    $("#slide_content").html("Loading");

    getSuggests(component,'popup');

    $('#slide').popup({
        outline: true, // optional
        focusdelay: 400, // optional
        vertical: 'center', //optional
        autoopen:true,

    });
};

var setDetails = function(component, type){

    // console.log("setDetails-" + component + '-' + type);
    $('#detail_view_img').attr("src","./images/wso2/" + component + ".png");
    $('#detail_view_title').text(product_details[component].title)
    if(type === 'toolbox'){;
        $('#detail_view_content').html(product_details[component].description);
        getSuggests(component, 'toolbox');
    }
    else if(type==='graph'){
        $('#detail_view_content').html("Here are some suggestions to connect "+product_details[component].title +".<br/><br/>")
        $('#links_details').html("");
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
        "<table><tr><td style='width: 80px;'><img class='suggest_img'  src='"+"./images/wso2/"+component+".png' /></td>" +
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
        // console.log( 'click');
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
        mouseDown: function(sender, me) {
            // console.log('mouseDown');
        },
        mouseMove: function(sender, me) {
            // console.log('mouseMove');
        },
        mouseUp: function(sender, me) {
            // console.log('mouseup');
            if (me.state != null) {
                setDetails(me.state.cell.getStyle(), 'graph');
            }
            //listener for click on edges
            if (me.sourceState != null && me.sourceState.cell.isEdge() == true) {
                // console.log("clicked on edge");
            }

        },
        dragEnter: function(evt, state) {
            // console.log('dragEnter');
        },
        dragLeave: function(evt, state) {
            // console.log('dragLeave');
        }
    });
}

var getSuggests = function (component, place) {
    $.getJSON( "http://10.100.4.196:5000/article_suggest/"+component, function( data ) {
        // $( ".result" ).html( data );
        // if(Object.keys(data))
        var txtSlideContent = "<ul>";
        Object.keys(data).forEach(function (key) {
            txtSlideContent+="<li><a target='_blank' href='"+data[key]+"'>"+key+"</a></li>"
        });
        txtSlideContent+="</ul>"
        $("#slide_content").html(txtSlideContent);
        if(place=='toolbox')
            $('#links_details').html(txtSlideContent);
        else
            $('#links_details').html("");
        console.log(data )
    });
}

$(document).ready(function() {

    // Initialize the plugin
    $('#contact_popup').popup();

    $('.contact_popup_open').on('click', function (evt) {

        // var canvas = document.getElementById("#drawing_pad");
        // var img = canvas.toDataUR    L("image/png");
        $('#contact_popup_img').html("")
        var svg = $("svg").clone();
        $('#contact_popup_img').html(svg );
    })

    $('#final_form_submit').on('click', function (event) {
        event.preventDefault();
        var formData = {
            'name'              : $('#exampleName').val(),
            'email'             : $('#exampleInputEmail1').val(),
            'superheroAlias'    : $('input[name=superheroAlias]').val()
        };

        var rotiData = {
            "event": {
                "payloadData": {
                    "FirstName": $('#exampleName').val(),
                    "LastName": $('#exampleLast').val(),
                    "Email": $('#exampleInputEmail').val(),
                    "Title": $('#exampleJob').val(),
                    "Company": $('#exampleCompany').val(),
                    "Country": $('#exampleCountry').val(),
                    "Ipaddress": "199.119.233.228",
                    "Region": "ROW"
                }
            }
        };

        // process the form
        $.ajax({
            type        : 'POST', // define the type of HTTP verb we want to use (POST for our form)
            url         : 'http://10.100.4.196:9763/endpoints/input_listner', // the url where we want to POST
            data        : rotiData, // our data object
            dataType    : 'json', // what type of data do we expect back from the server
            encode          : true
        })
        // using the done promise callback
        .done(function(data) {

            // log data to the console so we can see
            console.log(data);

            // here we will handle errors and validation messages
        });

        setTimeout(function () {
            $('#contact_popup').popup( 'hide');
            $('#success_popup').popup('show');
            setTimeout(function () {
                $('#success_popup').popup('hide');
            }, 1000)
        }, 500);
    })
});

var generateGraph = function (configXML, editor) {
    var graphNode = editor.graph.container;
    graphNode.style.display = '';

    var doc = mxUtils.parseXml(configXML);
    var dec = new mxCodec(doc);
    dec.decode(doc.documentElement, editor.graph.getModel());

    // Makes sure nothing is selected in IE
    if (mxClient.IS_IE)
    {
        mxUtils.clearSelection();
    }
}