package semmed{
	import flare.animate.GraphTransitioner;
	import flare.data.DataSource;
    import flare.vis.Visualization;
    import flare.vis.data.Data;
    import flare.vis.operator.encoder.ColorEncoder;
    import flare.vis.operator.encoder.ShapeEncoder;
    import flare.vis.operator.layout.AxisLayout;
    import flare.vis.palette.ColorPalette;
	import flare.vis.util.Filters;

	import flash.display.Graphics;
    import flash.display.Sprite;
    import flash.events.Event;
    import flash.geom.Rectangle;
    import flash.net.URLLoader;
	import flare.animate.Sequence;
	import flare.animate.Transitioner;
	import flare.util.Button;
	import flare.util.GraphUtil;
	import flare.vis.Visualization;
	import flare.vis.controls.ExpandControl;
	import flare.vis.controls.DragControl;
	import flare.vis.data.Data;
	import flare.vis.data.NodeSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.operator.OperatorSwitch;
	import flare.vis.operator.layout.CircleLayout;
	import flare.vis.operator.layout.ForceDirectedLayout;
	import flare.vis.operator.layout.IndentedTreeLayout;
	import flare.vis.operator.layout.Layout;
	import flare.vis.operator.layout.NodeLinkTreeLayout;
	import flare.vis.operator.layout.RadialTreeLayout;
	import flare.vis.util.graphics.Shapes;


	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	import mx.core.UIComponent;

	import semmed.jung.SpringLayout;
	import flare.vis.data.DataList;

	[SWF(width="800", height="600", backgroundColor="#ffffff", frameRate="30")]
	public class GraphVisualizer extends UIComponent
	{

		public var dataF:Data;

		private var vis:Visualization;

		private var spring:ForceDirectedLayout;

		private var tension:Number;

		private var os:OperatorSwitch;

		public var allEdges:Array = new Array();

		public var allNodes:Array = new Array();

		private var t:Transitioner;

		public function play():void
		{
			var os:OperatorSwitch = vis.operators.getOperatorAt(0) as OperatorSwitch;
			if (os.index == 0)
				vis.continuousUpdates = true;
			else
				vis.update();
		}

		public function stop():void
		{
			vis.continuousUpdates = false;
			redraw();
		}

        public function GraphVisualizer()
        {
            //loadData();
         	//name = "GraphView";
			var w:Number = width;
			var h:Number = height;

			dataF = new Data();//GraphUtil.diamondTree(3,3,3);
			vis = new Visualization(dataF);
			vis.bounds = new Rectangle(0,0,w,h);
        }

        public function setRoot(n:NodeSprite):void{
        	(os.getOperatorAt(1) as NodeLinkTreeLayout).layoutRoot = n;
			(os.getOperatorAt(3) as RadialLayout).layoutRoot = n;
        	//(os.getOperatorAt(2) as IndentedTreeLayout).layoutRoot = n;
        	//(os.getOperatorAt(2) as RadialTreeLayout).layoutRoot = n;
        }


        public function loadData():void
        {
        	spring = new ForceDirectedLayout();
			spring.defaultSpringLength = 100;
			tension = spring.defaultSpringTension;

        	os = new OperatorSwitch(
				spring,
				new NodeLinkTreeLayout(Layout.LEFT_TO_RIGHT, 20, 5, 10),
				new CircleLayout(),
				new RadialLayout()
			);
			var anchors:Array = [
				null,
				new Point(0, 0),
				new Point(0, 0),
				new Point(0, 0)
			];
			os.index = 1;
			vis.marks.x = anchors[1].x;
			vis.marks.y = anchors[1].y;

			//(os[0] as ForceDirectedLayout).defaultSpringLength = 10;
			//(os[0] as ForceDirectedLayout).defaultSpringTension = 0.001;

			(os[2] as CircleLayout).layoutBounds = new Rectangle(0,0,width,height);

			vis.operators.add(os);
			vis.tree.nodes.visit(function(n:NodeSprite):Boolean {
				n.fillColor = 0xaaaaaa; n.fillAlpha = 0.5;
				n.lineColor = 0xdddddd; n.lineAlpha = 0.8;
				n.lineWidth = 1;
				return (n.buttonMode = true);
			});
			vis.update();
			addChild(vis);

//			new DragControl(flare.vis.util.Filters.isNodeSprite).attach(vis);
			//new ExpandControl(vis);
//			new DragControl(vis);
        }

        public function filter(predicates:Array,semtypes:Array):void{
        	//var l:DataList = new DataList();

	        for each (var v:NodeSprite in allNodes)
	        	v.visible = false;

        	for each (var e:EdgeSprite in allEdges)
        		dataF.removeEdge(e);

        	for each (var x:EdgeSprite in allEdges){
        		/* if (isContained(predicates, x.data.predicate) && isContained(semtypes, x.source.data.semtype) &&
				    isContained(semtypes,x.target.data.semtype)){ */
				// 07/17/2008, Dongwook Shin, SemType is ignored for the time being
				if (isContained(predicates, x.data.predicate)){
        			dataF.addEdge(x);
        			x.source.visible = true;
        			x.target.visible = true;
        		}
        	}
        }

        public function filterCitations(citations:Object,predicates:Array,semtypes:Array):void{
        	for each (var v:NodeSprite in allNodes)
	        	v.visible = false;

	        for each (var e:EdgeSprite in allEdges)
        		dataF.removeEdge(e);

        	for each (var x:EdgeSprite in allEdges)
        		if (isContained(predicates,x.data.predicate) && isContained(semtypes, x.source.data.semtype) &&
				    isContained(semtypes,x.target.data.semtype))
	        		for each (var z:String in (x.data.citation as String).split(","))
		        		if (isLabelContained(citations,z)){
	    	    			dataF.addEdge(x);

    	    				x.source.visible = true;
        					x.target.visible = true;
        					break;
        				}
        }

        public function isContained(x:Array,y:String):Boolean{
        	for each(var z:String in x)
        		if (z==y)
        			return true;
        	return false;
        }

        public function isLabelContained(x:Object,y:String):Boolean{
        	for each(var z:Object in x){
        		var l:String = new String(z.data);
        		if (l==y)
        			return true;
        	}
        	return false;
        }

        public function changeLayout(idx:int,animate:Boolean):void {
//			var nonVisibleNodes:Array = new Array();
//			for each(var n:NodeSprite in dataF.nodes)
//				if (!n.visible)
//					nonVisibleNodes.push(n);
        	vis.continuousUpdates = false;
        	vis.operators[0].index = idx;
			clearEdges();
			//if (t)
			//	t.doEnd();
        	if (idx > 0) {
				if (animate)
					t = new GraphTransitioner(this, 0.3);
				else
					t = null;
				if (t!=null)
					if (idx==2){
						t.$(vis.marks).x = 0;
						t.$(vis.marks).y = 0;
					}else {
						t.$(vis.marks).x = 0;// width / 2;
						t.$(vis.marks).y = 0;// height / 2;
						//t.$(vis.marks).x = center.x-(os.getOperatorAt(1) as NodeLinkTreeLayout).layoutRoot.parent.x-(os.getOperatorAt(1) as NodeLinkTreeLayout).layoutRoot.x;// width / 2;
						//t.$(vis.marks).y = center.y;//height/2;
					}
				if (t)
					vis.update(t).play();
				else
					vis.update();
	        }else {
				if (vis.data.nodes.size > 100){
				//	spring.defaultSpringTension = tension / 4;
				//else if (vis.data.nodes.size > 150) {
				//	vis.operators[0].index = 1;
					//if (t)
					//	vis.update(t).play();
					//else
					//	vis.update();
				//	return;
					//spring.defaultSpringTension = tension / 20;
					//spring.defaultSpringDamping *= 2;
				}else
					spring.defaultSpringTension = tension;
	        	//spring.update(vis.data);
	        	vis.continuousUpdates = true;
	        }
//			for each(n in nonVisibleNodes)
//				n.visible = false;
		}

		public function glowEdge(edge:EdgeSprite):void {
			for each (var e:EdgeSprite in dataF.edges)
        		e.data.glow = null;
			edge.data.glow = true;
			redraw();
		}

        public function glow(items:Array):void{
        	for each (var e:EdgeSprite in dataF.edges){
        		e.data.glow=null;
	        	for each(var pmid:Object in items)
    	    		for each (var z:String in (e.data.citation as String).split(","))
			       		if (z==new String(pmid.data)){
			       			e.data.glow = true;
			       			break;
        				}

        	}
        	redraw();
        }

		public function unglow():void{
        	for each (var e:EdgeSprite in dataF.edges)
        		e.data.glow=null;
        	redraw();
        }

		public function clearEdges():void {
			for each(var e:EdgeSprite in dataF.edges) {
				var g:Graphics = e.graphics;
				g.clear();
			}
		}

        public function redraw():void {
			for each(var e:EdgeSprite in dataF.edges)
				EdgeRenderer.manualRender(e);
        }

	}
}
