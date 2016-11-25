/**
 * Created by wso2123 on 11/25/16.
 */


var showDialog  =  function() {
    // alert("dffsg")
    $('#slide').popup({
        outline: true, // optional
        focusdelay: 400, // optional
        vertical: 'top', //optional
        autoopen:true
    });
};

// var mouseLoc = [0,0]
//
// window.onmousemove = function (e) {
//     loc[0] = e.clientX;
//     loc[1] = e.clientY;
// };

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
                    showDialog();
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
}