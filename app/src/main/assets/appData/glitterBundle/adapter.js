"use strict";
//行式列表
function createLinearAdapter(map,finish){
    var adapter={
        spanCount:0,
        reactView: undefined,
        elementId: '',
        item:[],
        componentDidMount:function (pos){},
        ListItem:createReactClass({
            render: function () {
                var that = this
                return <div id={`listItem` + that.props.index}
                            dangerouslySetInnerHTML={{__html: `${adapter.onBindViewHolder(that.props.data,that.props.index)}`}}></div>
            },
            componentDidMount: function () {
                adapter.componentDidMount(this.props.index)
                let index = this.props.index
                let height = $(`#listItem${(index)}`).height()
                if (height !== undefined) {
                    adapter.changeItemHeight(index, height)
                }
            },
            componentDidUpdate:function (){
                adapter.componentDidMount(this.props.index)
                let index = this.props.index
                let height = $(`#listItem${(index)}`).height()
                if (height !== undefined) {
                    adapter.changeItemHeight(index, height)
                }
            }
        }),
        InfiniteList: createReactClass({
            getInitialState: function () {
                return {
                    isInfiniteLoading: false,
                    item:adapter.item.map(function (data){
                        return {elementHeight:100,data:data}
                    })
                };
            },
            generateVariableelementHeight: function (number, height) {
                var heights = [];
                for (var i = 0; i < number; i++) {
                    heights.push(height);
                }
                return heights;
            },
            handleInfiniteLoad: function () {
                if (this.state.isInfiniteLoading||adapter.isBtn) {
                    return
                }
                adapter.reactView=this
                this.setState({
                    isInfiniteLoading: true
                });
                adapter.getNewData(function (data) {
                    adapter.item=adapter.item.concat(data)
                    adapter.reactView.setState({
                        item: adapter.item.map(function (data){return {elementHeight:100,data:data}}),
                        isInfiniteLoading:false
                    })
                },this)
            },
            elementInfiniteLoad: function () {
                if (adapter.isBtn) {
                    return ``
                }
                return <div dangerouslySetInnerHTML={{__html: `${adapter.getLoadingView()}`}}></div>;
            },
            render: function () {
                adapter.reactView = this
                let that = this
                adapter.changeItemHeight = function (index, height) {
                    if (that.state.item[index].elementHeight === height) {
                        return
                    }
                    that.state.item[index].elementHeight  = height
                    that.setState({});
                }
                if(adapter.isBtn&&(that.state.item.length===0)){
                    return <div dangerouslySetInnerHTML={{
                        __html: adapter.emptyView()
                    }}></div>
                }
                var elements = this.state.item.map(function (el, i) {
                    return <adapter.ListItem key={i} index={i}  height={el.elementHeight} lineHeight={el.elementHeight + "px"}
                                             data={el.data}/>;
                })
                function getElementHeights(){
                    var dd=[]
                    that.state.item.map(function (data){dd.push(data.elementHeight)})
                    return dd
                }
                return <Infinite
                    elementHeight={getElementHeights()}
                    containerHeight={$(`#${adapter.elementId}`).height()}
                    infiniteLoadBeginEdgeOffset={200}
                    onInfiniteLoad={this.handleInfiniteLoad}
                    loadingSpinnerDelegate={this.elementInfiniteLoad()}
                    isInfiniteLoading={this.state.isInfiniteLoading}
                    timeScrollStateLastsForAfterUserScrolls={200}
                >
                    {elements}
                </Infinite>;
            }
        }),
        onBindViewHolder: function (data,index) {
            return ``
        },
        getNewData: function (callback) {

        },
        setItems:function (items){
            adapter.item=items
            adapter.reactView.setState({
                isInfiniteLoading:false,
                item:adapter.item.map(function (data){return {elementHeight:100,data:data}})
            })
        },
        isBtn:false,
        getLoadingView:function (){
            return ``
        },
        changeItemHeight:function (){},
        clear:function (){
            adapter.item=[]
            adapter.isBtn=false
            adapter.reactView.setState({
                isInfiniteLoading: false,
                item: []
            })
        },
        getItems:function (){
            return adapter.item
        },
        getItem:function (index){
            return adapter.item[index]
        },
        notifyDataSetChange:function (){
            adapter.reactView.setState({
                item: adapter.item.map(function (data){return {elementHeight:100,data:data}})
            })
        },
        emptyView:function (){
            return ``
        }
    }
    map.adapter=adapter
    adapter.item=map.item
    if(map.emptyView){
        adapter.emptyView=map.emptyView
    }
    adapter.elementId=map.id
    adapter.onBindViewHolder=map.onBindViewHolder;
    adapter.getNewData=map.getNewData
    if(map.getLoadingView){adapter.getLoadingView=map.getLoadingView}
    if(map.componentDidMount){adapter.componentDidMount=map.componentDidMount}
    setTimeout(function (){
        ReactDOM.render(<adapter.InfiniteList/>, document.getElementById(adapter.elementId));
        if(finish){
            finish()
        }
    },20)
    return adapter
}
//網格式列表
function createGridAdapter(map,finish){
    var adapter={
        spanCount:0,
        reactView: undefined,
        elementId: '',
        componentDidMount:function (pos){

        },
        ListItem:createReactClass({
            render: function () {
                var that = this
                var itemList=''
                // for(var a=0;a<adapter.spanCount;a++){
                //     let  index=(that.props.index*adapter.spanCount)+a
                //     itemList +=`<div style="flex: auto;"></div>`
                //     if(that.props.dataList[index]){
                //         itemList+=(adapter.onBindViewHolder(that.props.dataList[index]['data'],index))
                //     }
                //     itemList +=`<div style="flex: auto;"></div>`
                //
                // }
                for(var a=0;a<adapter.spanCount;a++){
                    let  index=(that.props.index*adapter.spanCount)+a

                    if(that.props.dataList[a]){
                        itemList+=`<div style="width: calc(100% / ${adapter.spanCount});display: flex;align-items: center;justify-content: center;">${(adapter.onBindViewHolder(that.props.dataList[a].data,index))}</div>`
                    }
                }
                console.log(`第${that.props.index}行-數據${itemList}`)
                return <div id={`listItem` + that.props.index}
                            dangerouslySetInnerHTML={{__html: `<div style="display: flex;width: 100%;justify-content: start;align-items: center;">${itemList}</div>`}}></div>
            },
            componentDidMount: function () {
                adapter.componentDidMount(this.props.index)
                let index = this.props.index
                let height = $(`#listItem${(index)}`).height()
                if (height !== undefined) {
                    adapter.changeItemHeight(index, height)
                }
            },
            componentDidUpdate:function (){
                adapter.componentDidMount(this.props.index)
                let index = this.props.index
                let height = $(`#listItem${(index)}`).height()
                if (height !== undefined) {
                    adapter.changeItemHeight(index, height)
                }
            }
        }),
        InfiniteList: createReactClass({
            getInitialState: function () {
                return {
                    isInfiniteLoading: false,
                    item:adapter.item.map(function (data){
                        return {elementHeight:100,data:data}
                    })
                };
            },
            generateVariableelementHeight: function (number, height) {
                var heights = [];
                for (var i = 0; i < number; i++) {
                    heights.push(height);
                }
                return heights;
            },
            handleInfiniteLoad: function () {
                if (this.state.isInfiniteLoading||adapter.isBtn) {
                    return
                }
                adapter.reactView=this
                this.setState({
                    isInfiniteLoading: true
                });
                adapter.getNewData(function (data) {
                    adapter.item=adapter.item.concat(data)
                    adapter.reactView.setState({
                        item: adapter.item.map(function (data){return {elementHeight:100,data:data}}),
                        isInfiniteLoading:false
                    })
                },this)
            },
            elementInfiniteLoad: function () {
                if (adapter.isBtn) {
                    return ``
                }
                return <div dangerouslySetInnerHTML={{__html: `${adapter.getLoadingView()}`}}></div>;
            },
            setItems:function (items){
                adapter.item=items
                adapter.reactView.setState({
                    isInfiniteLoading:false,
                    item:adapter.item.map(function (data){return {elementHeight:100,data:data}})
                })
            },
            render: function () {
                adapter.reactView = this
                let that = this
                adapter.changeItemHeight = function (index, height) {
                    if (that.state.item[index].elementHeight === height) {
                        return
                    }
                    that.state.item[index].elementHeight  = height
                    that.setState({});
                }
                if(adapter.isBtn&&(that.state.item.length===0)){
                    return <div dangerouslySetInnerHTML={{
                        __html: adapter.emptyView()
                    }}></div>
                }
                var rowItem = adapter.spanCount
                var elements = []
                for(var i=0;i<Math.ceil(that.state.item.length/rowItem);i++){
                    var dataList=[]
                    for(var a=0;a<rowItem;a++){
                        var data=that.state.item[(i*rowItem)+a]
                        dataList.push(data)
                    }
                    var el=that.state.item[(i)]
                    elements.push( <adapter.ListItem key={i} index={i}  height={el.elementHeight} lineHeight={el.elementHeight + "px"} dataList={dataList}/>)
                }
                function getElementHeights(){
                    var dd=[]
                    for(var i=0;i<Math.ceil(that.state.item.length/rowItem);i++){
                        var el=that.state.item[i]
                        dd.push( el.elementHeight)
                    }

                    return dd
                }
                // alert('elements數量-'+elements.length)
                // alert('getElementHeights數量-'+getElementHeights().length)
                return <Infinite
                    elementHeight={getElementHeights()}
                    containerHeight={$(`#${adapter.elementId}`).height()}
                    infiniteLoadBeginEdgeOffset={200}
                    onInfiniteLoad={this.handleInfiniteLoad}
                    loadingSpinnerDelegate={this.elementInfiniteLoad()}
                    isInfiniteLoading={this.state.isInfiniteLoading}
                    timeScrollStateLastsForAfterUserScrolls={200}
                >
                    {elements}
                </Infinite>;
            }
        }),
        onBindViewHolder: function (data,index) {
            return ``
        },
        getNewData: function (callback) {

        },
        isBtn:false,
        getLoadingView:function (){
            return ``
        },
        changeItemHeight:function (){},
        clear:function (){
            adapter.item=[]
            adapter.isBtn=false
            adapter.reactView.setState({
                isInfiniteLoading: false,
                item: []
            })
        },
        getItems:function (){
            return adapter.item
        },
        getItem:function (index){
            return adapter.item[index]
        },
        notifyDataSetChange:function (){
            adapter.reactView.setState({
                item: adapter.item.map(function (data){return {elementHeight:100,data:data}})
            })
        },
        emptyView:function (){
            return ``
        }
    }
    map.adapter=adapter
    adapter.item=map.item
    if(map.emptyView){
        adapter.emptyView=map.emptyView
    }
    adapter.elementId=map.id
    adapter.spanCount=map.spanCount
    adapter.onBindViewHolder=map.onBindViewHolder;
    adapter.getNewData=map.getNewData
    if(map.getLoadingView){adapter.getLoadingView=map.getLoadingView}
    if(map.componentDidMount){adapter.componentDidMount=map.componentDidMount}
    setTimeout(function (){
        ReactDOM.render(<adapter.InfiniteList/>, document.getElementById(adapter.elementId));
        if(finish){
            finish()
        }
    },20)
    return adapter
}


