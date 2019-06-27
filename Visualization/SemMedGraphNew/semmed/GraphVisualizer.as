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
	import flare.vis.util.Transforms;
	import flare.vis.operator.distortion.FisheyeDistortion;
	import flare.vis.controls.PanZoomControl;
	import flare.vis.controls.AnchorControl;
	import flare.vis.palette.ShapePalette;
	import flare.vis.operator.encoder.PropertyEncoder;

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
	import flash.text.TextField;
	import flash.text.TextFormat;
	import flare.vis.operator.distortion.BifocalDistortion;
    import mx.controls.Alert;

	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	import mx.core.UIComponent;

	import semmed.jung.SpringLayout;
	import semmed.NodeRenderer;
	import flare.vis.data.DataList;

	[SWF(width="800", height="600", backgroundColor="#ffffff", frameRate="30")]
	public class GraphVisualizer extends UIComponent
	// public class GraphVisualizer extends Sprite
	{

		public var dataF:Data;

		private var vis:Visualization;

		private var spring:ForceDirectedLayout;

		private var distort:Layout;

		private var tension:Number;

		private var os:OperatorSwitch;

		public var allEdges:Array = new Array();

		public var allNodes:Array = new Array();

		private var t:Transitioner;
		private var previousIdx:Number =1;
		private var rootNode:NodeSprite;

		public var shapePalette:ShapePalette = ShapePalette.defaultPalette();

		public var defaultSize:Number = 6;

		/* public function play():void
		{
			var os:OperatorSwitch = vis.operators.getOperatorAt(0) as OperatorSwitch;
			if (os.index == 0)
				vis.continuousUpdates = true;
			else
				vis.update();
		}

		 public function playDistortion():void
		{
			vis.controls.add(new AnchorControl(distort));
		}

		public function stop():void
		{
			vis.continuousUpdates = false;
			redraw();
		} */

        public function GraphVisualizer()
        {
            //loadData();
         	//name = "GraphView";

			var w:Number = width;
			var h:Number = height;

			dataF = new Data();//GraphUtil.diamondTree(3,3,3);
			// vis = new Visualization(dataF);
			// vis.bounds = new Rectangle(0,0,w,h);
        }

        public function saveGraph():void {

        }

        public function reset():void {
			dataF = new Data();
        }

        public function setRoot(n:NodeSprite):void{
        	(os.getOperatorAt(1) as NodeLinkTreeLayout).layoutRoot = n;
        	(os.getOperatorAt(2) as CircleLayout).layoutRoot = n;
			(os.getOperatorAt(3) as RadialLayout).layoutRoot = n;
			rootNode = n;
        	// (os.getOperatorAt(4) as FisheyeDistortion).layoutRoot = n;
        	//(os.getOperatorAt(2) as RadialTreeLayout).layoutRoot = n;
        }


        public function loadData(alreadyRendered:String):void
        {
        	spring = new ForceDirectedLayout();
			spring.defaultSpringLength = 100;
			tension = spring.defaultSpringTension;
			vis = new Visualization(dataF);
			vis.bounds = new Rectangle(0,0,width,height);
        	os = new OperatorSwitch(
				spring,
				new NodeLinkTreeLayout(Layout.LEFT_TO_RIGHT, 20, 5, 10),
				new CircleLayout(),
				new RadialLayout(),
				// new AxisLayout() // For already rendered graph
				new FixedLayout() // For already rendered graph
				// new FisheyeDistortion(4,4,2)
				// new IndentedTreeLayout(20)
			);
			var anchors:Array = [
				null,
				new Point(0, 0),
				new Point(0, 0),
				new Point(0, 0)
				// new Point(0, 0)
			];
			if(alreadyRendered == "yes")
			 	os.index = 4;
			else
				os.index = 1;
			// os.index = 4;
			vis.marks.x = anchors[1].x;
			vis.marks.y = anchors[1].y;

			//(os[0] as ForceDirectedLayout).defaultSpringLength = 10;
			//(os[0] as ForceDirectedLayout).defaultSpringTension = 0.001;

			(os[2] as CircleLayout).layoutBounds = new Rectangle(0,0,width,height);
			// os[3].layoutAnchor = anchors[4];
			// (os[3] as FisheyeDistortion).layoutBounds = new Rectangle(0,0,width,height);
			// vis.operators.add(new PropertyEncoder({scaleX:1, scaleY:1}));

			// vis.operators.add(new NodeLinkTreeLayout());
			// vis.operators.add(new PropertyEncoder({scaleX:1, scaleY:1}));
			vis.operators.add(os);
			// vis.operators.add(new FisheyeDistortion(4,4,2));
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
			// new PanZoomControl(vis.marks);
			// vis.continuousUpdates = true;
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
        		/* if (isContained(predicates,x.data.predicate) && isContained(semtypes, x.source.data.semtype) &&
				    isContained(semtypes,x.target.data.semtype)) */
	        	// 07/17/2008, Dongwook Shin, SemType is ignored for the time being
				if (isContained(predicates, x.data.predicate))
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
			var radial:Layout;
        	if(previousIdx != idx) {
				if(idx == 5) {
				   // vis.operators.clear();
				    removeChild(vis);
					vis = null;
					vis = new Visualization(dataF);
					vis.bounds = new Rectangle(0,0,width,height);

					vis.operators.add(new PropertyEncoder({scaleX:1, scaleY:1}));
					vis.operators.add(radial = new NodeLinkTreeLayout());
					// vis.operators.add(distort = new FisheyeDistortion(1,1,2));

					vis.operators.add(distort = new BifocalDistortion(0.1, 2.0, 0.1, 2.0));
					// vis.operators.add(new FisheyeTreeFilter());
					// vis.operators.add(fisheyeos);
					// distort = fisheyeos.getOperatorAt(0) as Layout;
					radial.layoutAnchor = new Point(0, 0);
					radial.layoutRoot = rootNode;
					// setDistortion();
					addChild(vis);
					// vis.controls.add(new AnchorControl(distort));
					previousIdx = 5;
					// playDistortion();

				} else {
					if(previousIdx == 5) {
						vis.operators.clear();
						removeChild(vis);
						vis = null;
						vis = new Visualization(dataF);
						vis.bounds = new Rectangle(0,0,width,height);
						os = new OperatorSwitch(
						spring,
						// new NodeLinkTreeLayout(Orientation.LEFT_TO_RIGHT, 20, 5, 10),
							new NodeLinkTreeLayout(Layout.LEFT_TO_RIGHT, 20, 5, 10),
							new CircleLayout(),
							new RadialLayout(),
							// new AxisLayout() // For already rendered graph
							new FixedLayout() // For already rendered graph
						);
						var anchors:Array = [
							null,
							new Point(0, 0),
							new Point(0, 0),
							new Point(0, 0),
							new Point(0, 0)
						];
						vis.marks.x = anchors[1].x;
						vis.marks.y = anchors[1].y;
						vis.operators.add(os);
						os.index = idx;
						addChild(vis);
					}
					// vis.update().playRegular();
					play();
				}
			}
			vis.update();
		}


		private function updateMouse(evt:Event):void
		{
			// get current anchor, run update if changed
			var p1:Point = distort.layoutAnchor;
			distort.layoutAnchor = new Point(vis.mouseX, vis.mouseY);
			// distortion might snap the anchor to the layout bounds
			// so we need to re-retrieve the point to get an accurate point
			var p2:Point = distort.layoutAnchor;
			if (p1.x != p2.x || p1.y != p2.y) vis.update();
		}

		public function play():void {
			addEventListener(Event.ENTER_FRAME, updateMouse, false, 0, true);
		}

		public function stop():void {
			removeEventListener(Event.ENTER_FRAME, updateMouse);
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

        public function zoom(scale:int):void{
        	for each (var e:EdgeSprite in dataF.edges){
        		e.lineWidth = e.lineWidth/scale;
        		// e.arrowHeight = e.arrowHeight/scale;
        		e.arrowHeight = -1;
        		e.arrowWidth = -1;
        		// e.arrowWidth = e.arrowWidth/scale;
        		// e.scaleX = 1/scale;
				// e.scaleY = 1/scale;
				// Transforms.zoomBy(e, scale, 0, 0);
        		// e.size = e.size/scale;
        	}
			// var tr:Transforms = new Transforms();

        	for each(var n:NodeSprite in dataF.nodes) {
				n.scaleX = 1/scale;
				n.scaleY = 1/scale;
				// var orenderer:NodeRenderer = n.renderer as NodeRenderer;
				// orenderer.setZoom(scale);
				// n.renderer = orenderer;
				// n.render();
				 // n.radius = 1000;
				// var size:Number = n.size * defaultSize;
				// var g:Graphics = n.graphics;
				// g.drawCircle(0,0,size/scale);
				// shapePalette.getShape(n.shape)(g, size/scale);
				// var t:TextField = n.getChildAt(0) as TextField;
				// var tf:TextFormat = new TextFormat();
				// tf.size = 12;
				// t.setTextFormat(tf);
 				// Transforms.zoomBy(n, 1/scale, 0, 0);
        	}

			// Transforms.zoomBy(this, scale, 0, 0);
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

		public function clearGraph():void {
			for each(var e:EdgeSprite in dataF.edges) {
				var g:Graphics = e.graphics;
				g.clear();
			}
			for each(var n:NodeSprite in dataF.nodes) {
				var g:Graphics = n.graphics;
				g.clear();
				n.removeChildAt(0);
			}
			// dataF.clear();
			// Alert.show("graph cleared");
			dataF= null;
			dataF = new Data();
			// Alert.show("dataF reset");
		}

        public function redraw():void {
			for each(var e:EdgeSprite in dataF.edges)
				EdgeRenderer.manualRender(e);
        }

	}
}
