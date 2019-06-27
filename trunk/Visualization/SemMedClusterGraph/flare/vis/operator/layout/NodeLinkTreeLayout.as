package flare.vis.operator.layout
{
	import flare.vis.operator.Operator;
	import flare.vis.data.NodeSprite;
	import flash.geom.Point;
	import flare.animate.Transitioner;
	import flare.util.Arrays;
	import mx.controls.Alert;

	/**
	 * Layout that places nodes using a tidy layout of a node-link tree
	 * diagram. This algorithm lays out a rooted tree such that each
	 * depth level of the tree is on a shared line. The orientation of the
	 * tree can be set such that the tree goes left-to-right (default),
	 * right-to-left, top-to-bottom, or bottom-to-top.
	 *
	 * <p>The algorithm used is that of Christoph Buchheim, Michael Jünger,
	 * and Sebastian Leipert from their research paper
	 * <a href="http://citeseer.ist.psu.edu/buchheim02improving.html">
	 * Improving Walker's Algorithm to Run in Linear Time</a>, Graph Drawing 2002.
	 * This algorithm corrects performance issues in Walker's algorithm, which
	 * generalizes Reingold and Tilford's method for tidy drawings of trees to
	 * support trees with an arbitrary number of children at any given node.</p>
	 */
	public class NodeLinkTreeLayout extends Layout
	{
		// -- Properties ------------------------------------------------------

		/** Property name for storing parameters for this layout. */
		public static const PARAMS:String = "nodeLinkTreeLayoutParams";

		private var _orient:uint = LEFT_TO_RIGHT; // the orientation of the tree
		private var _bspace:Number = 5;  // the spacing between sibling nodes
    	private var _tspace:Number = 25; // the spacing between subtrees
    	private var _dspace:Number = 50; // the spacing between depth levels
    	private var _depths:Array = new Array(20); // stores depth co-ords
    	private var _maxDepth:int = 0;
    	private var _ax:Number, _ay:Number; // for holding anchor co-ordinates
		private var _t:Transitioner; // temp variable for transitioner access

		private var _max_ax:Number;

		/** The orientation of the layout. */
		public function get orientation():uint { return _orient; }
		public function set orientation(o:uint):void { _orient = o; }

		/** The space between successive depth levels of the tree. */
		public function get depthSpacing():Number { return _dspace; }
		public function set depthSpacing(s:Number):void { _dspace = s; }

		/** The space between siblings in the tree. */
		public function get breadthSpacing():Number { return _bspace; }
		public function set breadthSpacing(s:Number):void { _bspace = s; }

		/** The space between different sub-trees. */
		public function get subtreeSpacing():Number { return _tspace; }
		public function set subtreeSpacing(s:Number):void { _tspace = s; }


		// -- Methods ---------------------------------------------------------

		/**
		 * Creates a new NodeLinkTreeLayout.
		 * @param orientation the orientation of the layout
		 * @param depthSpace the space between depth levels in the tree
		 * @param breadthSpace the space between siblings in the tree
		 * @param subtreeSpace the space between different sub-trees
		 */
		public function NodeLinkTreeLayout(orientation:uint=LEFT_TO_RIGHT,
			depthSpace:Number=50, breadthSpace:Number=5, subtreeSpace:Number=25)
		{
			_orient = orientation;
			_dspace = depthSpace;
			_bspace = breadthSpace;
			_tspace = subtreeSpace;
		}

		/** @inheritDoc */
		public override function operate(t:Transitioner=null):void
		{
			// Alert.show('step 1 in NodelLinkTree::operate()');
			_t = t!=null ? t : Transitioner.DEFAULT; // set transitioner
			// Alert.show('step 2 in NodelLinkTree::operate()');
        	Arrays.fill(_depths, 0);
        	_maxDepth = 0;
        	// Alert.show('step 3 in NodelLinkTree::operate()');
        	var a:Point = layoutAnchor;
        	//_ax = a.x; _ay = a.y;
			_ax = 0; _ay = 0;
			_max_ax = _ax;
			// Alert.show('step 4 in NodelLinkTree::operate()');
			for each(var node:NodeSprite in visualization.data.nodes)
				node.data.visited = false;

		//	trace(countNonVisited());
		// Alert.show('step 5 in NodelLinkTree::operate()');
		//	var root:NodeSprite = layoutRoot as NodeSprite;

			// Alert.show('step 5 in NodelLinkTree::operate()');
			var root:NodeSprite = findNextRoot();
			Alert.show('step 5.2 in NodelLinkTree::operate()');
			while(root!=null){
				trace("my seed is " + root.data.name);
				var thisroot:String = "my root = " + root.data.name;
				// Alert.show(thisroot);
				var rp:Params = params(root);

				// do first pass - compute breadth information, collect depth info
				firstWalk(root, 0, 1);

				// sum up the depth info
				determineDepths();

				// do second pass - assign layout positions
				secondWalk(root, null, -rp.prelim, 0, true);
				root = findNextRoot();

				_ax = _max_ax+100;
				_ay += 100;

			}
        	// Alert.show('step 6 in NodelLinkTree::operate()');
        	updateEdgePoints(_t);
        	_t = null; // clear transitioner reference
    	}

		private function findNextRoot():NodeSprite {
			// Alert.show('in findNextRoot()');
			if (!(layoutRoot as NodeSprite).data.visited && (layoutRoot as NodeSprite).visible) {
				// Alert.show('found root in findNextRoot()');
				return layoutRoot as NodeSprite;
			} for each(var node:NodeSprite in visualization.data.nodes) {
				// Alert.show(node.data.name);
				if (!node.data.visited && node.visible)
					return node;
			}
			// Alert.show('no node visit');
			return null;
		}

		private function countNonVisited():Number {
			var total:Number = 0;
			for each(var node:NodeSprite in visualization.data.nodes)
				if (!node.data.visited)
					total++;
			return total;
		}

    	private function firstWalk(n:NodeSprite, num:int, depth:uint):void
    	{
			n.data.visited = true;

    		var np:Params = params(n);
    		np.number = num;
    		updateDepths(depth, n);

    		var expanded:Boolean = n.expanded;
    		//if (n.childDegree == 0 || !expanded) // is leaf
			if (childDegree(n) == 0 || !expanded) // is leaf
    		{
    			//var l:NodeSprite = n.prevNode;
				var l:NodeSprite = prevNode(n);
    			np.prelim = l==null ? 0 : params(l).prelim + spacing(l,n,true);
    		}
    		else if (expanded) // has children, is expanded
    		{
    			var midpoint:Number, i:uint;
    			//var lefty:NodeSprite = n.firstChildNode;
				var lefty:NodeSprite = firstChild(n);
    			//var right:NodeSprite = n.lastChildNode;
				var right:NodeSprite = lastChild(n);
    			var ancestor:NodeSprite = lefty;
    			var c:NodeSprite = lefty;

    			//for (i = 0; c != null; ++i, c = c.nextNode) {
				for (i = 0; c != null; ++i, c = nextNode(c)) {
					firstWalk(c, i, depth+1);
					ancestor = apportion(c, ancestor);
    			}
    			executeShifts(n);
    			midpoint = 0.5 * (params(lefty).prelim + params(right).prelim);

    			//l = n.prevNode;
				l = prevNode(n);
    			if (l != null) {
    				np.prelim = params(l).prelim + spacing(l,n,true);
    				np.mod = np.prelim - midpoint;
    			} else {
    				np.prelim = midpoint;
    			}
    		}
    	}

    	private function apportion(v:NodeSprite, a:NodeSprite):NodeSprite
    	{
    		//var w:NodeSprite = v.prevNode;
			var w:NodeSprite = prevNode(v);
    		if (w != null) {
    			var vip:NodeSprite, vim:NodeSprite, vop:NodeSprite, vom:NodeSprite;
    			var sip:Number, sim:Number, sop:Number, som:Number;

    			vip = vop = v;
    			vim = w;
    			vom = firstChild(vip.parentNode);
				//vom = vip.parentNode.firstChildNode;

    			sip = params(vip).mod;
    			sop = params(vop).mod;
    			sim = params(vim).mod;
    			som = params(vom).mod;

    			var shift:Number;
    			var nr:NodeSprite = nextRight(vim);
    			var nl:NodeSprite = nextLeft(vip);
    			while (nr != null && nl != null) {
    				vim = nr;
    				vip = nl;
    				vom = nextLeft(vom);
    				vop = nextRight(vop);
    				params(vop).ancestor = v;
    				shift = (params(vim).prelim + sim) -
    					(params(vip).prelim + sip) + spacing(vim,vip,false);

    				if (shift > 0) {
    					moveSubtree(ancestor(vim,v,a), v, shift);
    					sip += shift;
    					sop += shift;
    				}

    				sim += params(vim).mod;
                	sip += params(vip).mod;
                	som += params(vom).mod;
                	sop += params(vop).mod;

                	nr = nextRight(vim);
                	nl = nextLeft(vip);
            	}
            	if (nr != null && nextRight(vop) == null) {
                	var vopp:Params = params(vop);
                	vopp.thread = nr;
                	vopp.mod += sim - sop;
            	}
            	if (nl != null && nextLeft(vom) == null) {
                	var vomp:Params = params(vom);
                	vomp.thread = nl;
                	vomp.mod += sip - som;
                	a = v;
            	}
        	}
        	return a;
    	}

    	private function nextLeft(n:NodeSprite):NodeSprite
    	{
    		var c:NodeSprite = null;
        	if (n.expanded) c = firstChild(n);// n.firstChildNode;
        	return (c != null ? c : params(n).thread);
    	}

    	private function nextRight(n:NodeSprite):NodeSprite
    	{
    		var c:NodeSprite = null;
    		if (n.expanded) c = lastChild(n)//n.lastChildNode;
        	return (c != null ? c : params(n).thread);
    	}

		private function moveSubtree(wm:NodeSprite, wp:NodeSprite, shift:Number):void
		{
			var wmp:Params = params(wm);
			var wpp:Params = params(wp);
			var subtrees:Number = wpp.number - wmp.number;
			wpp.change -= shift/subtrees;
			wpp.shift += shift;
			wmp.change += shift/subtrees;
			wpp.prelim += shift;
			wpp.mod += shift;
		}

		private function executeShifts(n:NodeSprite):void
		{
			var shift:Number = 0, change:Number = 0;
			//for (var c:NodeSprite = n.lastChildNode; c != null; c = c.prevNode)
			for (var c:NodeSprite = lastChild(n); c != null; c = prevNode(c))
			{
				var cp:Params = params(c);
				cp.prelim += shift;
				cp.mod += shift;
				change += cp.change;
				shift += cp.shift + change;
			}
		}

		private function ancestor(vim:NodeSprite, v:NodeSprite, a:NodeSprite):NodeSprite
		{
			var vimp:Params = params(vim);
			var p:NodeSprite = v.parentNode;
			return (vimp.ancestor.parentNode == p ? vimp.ancestor : a);
		}

    	private function secondWalk(n:NodeSprite, p:NodeSprite, m:Number, depth:uint, visible:Boolean):void
    	{
    		// set position
    		var np:Params = params(n);
    		var o:Object = _t.$(n);
    		setBreadth(o, p, (visible ? np.prelim : 0) + m);
    		setDepth(o, p, _depths[depth]);
    		setVisibility(n, o, visible);

    		// recurse
    		var v:Boolean = n.expanded ? visible : false;
    		var b:Number = m + (n.expanded ? np.mod : np.prelim)
    		if (v) depth += 1;
    		//for (var c:NodeSprite = n.firstChildNode; c!=null; c=c.nextNode)
			for (var c:NodeSprite = firstChild(n); c!=null; c=nextNode(c))
    		{
    			secondWalk(c, n, b, depth, v);
    		}
    		np.clear();
    	}

		private function setBreadth(n:Object, p:NodeSprite, b:Number):void
		{
			switch (_orient) {
				case LEFT_TO_RIGHT:
				case RIGHT_TO_LEFT:
					n.y = _ay + b;
					break;
				case TOP_TO_BOTTOM:
				case BOTTOM_TO_TOP:
					n.x = _ax + b;
					break;
				default:
					throw new Error("Unrecognized orientation value");
			}
		}

		private function setDepth(n:Object, p:NodeSprite, d:Number):void
		{
			switch (_orient) {
				case LEFT_TO_RIGHT:
					n.x = _ax + d;

					if (n.x > _max_ax)
						_max_ax = n.x + 100;
					break;
				case RIGHT_TO_LEFT:
					n.x = _ax - d;
					break;
				case TOP_TO_BOTTOM:
					n.y = _ay + d;
					break;
				case BOTTOM_TO_TOP:
					n.y = _ax - d;
					break;
				default:
					throw new Error("Unrecognized orientation value");
			}
		}

		private function setVisibility(n:NodeSprite, o:Object, visible:Boolean):void
		{
			visible = true;
    		o.alpha = visible ? 1.0 : 0.0;
    		o.mouseEnabled = visible;
    		if (n.parentEdge != null) {
    			o = _t.$(n.parentEdge);
    			o.alpha = visible ? 1.0 : 0.0;
    			o.mouseEnabled = visible;
    		}

		}

		private function spacing(l:NodeSprite, r:NodeSprite, siblings:Boolean):Number
		{
			var w:Boolean = (_orient==BOTTOM_TO_TOP || _orient==TOP_TO_BOTTOM);
			return (siblings ? _bspace : _tspace) + 0.5 *
					(w ? l.width + r.width : l.height + r.height)
    	}

    	private function updateDepths(depth:uint, item:NodeSprite):void
    	{
    		var v:Boolean = (_orient==BOTTOM_TO_TOP || _orient==TOP_TO_BOTTOM);
    		var d:Number = v ? item.height : item.width;

			// resize if needed
			if (depth >= _depths.length) {
    			_depths = Arrays.copy(_depths, new Array(int(1.5*depth)));
    			for (var i:int=depth; i<_depths.length; ++i) _depths[i] = 0;
			}

        	_depths[depth] = Math.max(_depths[depth], d);
        	_maxDepth = Math.max(_maxDepth, depth);
    	}

    	private function determineDepths():void
    	{
        	for (var i:uint=1; i<_maxDepth; ++i)
            	_depths[i] += _depths[i-1] + _dspace;
    	}

		// -- Parameter Access ------------------------------------------------

		private function params(n:NodeSprite):Params
		{
			var p:Params = n.props[PARAMS] as Params;
			if (p == null) {
				p = new Params();
				n.props[PARAMS] = p;
			}
			if (p.number == -2) { p.init(n); }
			return p;
    	}

		private function prevNode(n:NodeSprite):NodeSprite {
			var nn:NodeSprite = n.prevNode;
			while (nn != null)
				if (nn.visible)
					return nn;
				else
					nn = nn.prevNode;
			return null;
		}

		private function nextNode(n:NodeSprite):NodeSprite {
			var nn:NodeSprite = n.nextNode;
			while (nn != null)
				if (nn.visible)
					return nn;
				else
					nn = nn.nextNode;
			return null;
		}

		private function firstChild(n:NodeSprite):NodeSprite {
			var nn:NodeSprite = n.firstChildNode;
			if (nn != null)
				if (nn.visible)
					return nn;
				else
					return nextNode(nn);
			else
				return null;
		}

		private function lastChild(n:NodeSprite):NodeSprite {
			var nn:NodeSprite = n.lastChildNode;
			if (nn != null)
				if (nn.visible)
					return nn;
				else
					return prevNode(nn);
			else
				return null;
		}

		private function childDegree(n:NodeSprite):Number {
			var i:Number = 0;
			var nn:NodeSprite = firstChild(n);
			while (nn != null){
				i++;
				nn = nextNode(nn);
			}
			return i;
		}
	} // end of class NodeLinkTreeLayout

}


import flare.vis.data.NodeSprite;

class Params {
	public var prelim:Number = 0;
	public var mod:Number = 0;
	public var shift:Number = 0;
	public var change:Number = 0;
	public var number:int = -2;
	public var ancestor:NodeSprite = null;
	public var thread:NodeSprite = null;

    public function init(item:NodeSprite):void
    {
    	ancestor = item;
    	number = -1;
    }

	public function clear():void
	{
		number = -2;
		prelim = mod = shift = change = 0;
		ancestor = thread = null;
	}
} // end of class Params