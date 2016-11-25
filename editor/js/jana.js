/**
 * Created by wso2123 on 11/25/16.
 */

$( function() {
    $( "#dialog" ).dialog({
        autoOpen: false,
        show: {
            effect: "blind",
            duration: 300
        },
        hide: {
            effect: "blind",
            duration: 500
        }
    });


} );

var showDialog  =  function() {

    $( "#dialog" ).dialog( "open" );
};

var mouseLoc = [0,0]

window.onmousemove = function (e) {
    loc[0] = e.clientX;
    loc[1] = e.clientY;
};