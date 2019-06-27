/**
* ...
* @author Default
* @version 0.1
*/

package semmed {

	import flare.vis.data.EdgeSprite;
	import flare.vis.operator.layout.Layout;
	import flare.vis.operator.Operator;
	import flare.vis.data.NodeSprite;
	import flash.geom.Point;
	import flare.animate.Transitioner;
	import flare.util.Arrays;
	
	public class RadialLayout extends Layout{
		
		
		private var nodesByLevel:Array;

		private var _t:Transitioner;
		
		public function RadialLayout() {
			
		}
				
		public override function operate(t:Transitioner=null):void
		{
			_t = (t!=null ? t : Transitioner.DEFAULT);
			
			var nodes:Array = new Array();
			
			for each(var n:NodeSprite in visualization.data.nodes)
				if (n.visible)
					nodes.push(new Node(n));
			sortByLevel(nodes);
			
			var root:Node = findRoot(nodes);
			
			while (root != null) {			
				root.x = root.node.x;
				root.y = root.node.y;
				computeWidth(root);		
				layout(root,true);
				root = findNextRoot(nodes);
			}			
			//if (t != null)
			updateEdgePoints(_t);
			_t = null;
		}
		
		private function findRoot(nodes:Array):Node {
			for each(var node:Node in nodes)
				if (node.node == layoutRoot)
					return node;
			return nodes[0];
		}
		
		private function findNextRoot(nodes:Array):Node {
			for each(var node:Node in nodes)
				if (!node.visited)
					return node;
			return null;
		}
			
		private function sortByLevel(nodes:Array):void {
		
			nodesByLevel = new Array();
			
			var root:Node = findRoot(nodes);
			
			var i:Number = 0;
			
			while (root != null) {		
				nodesByLevel.push(root);
				while (i < nodesByLevel.length) {
					var n:Node = nodesByLevel[i];
					n.visited = true;
					for each (var edge:EdgeSprite in visualization.data.edges) {
						if (edge.source == n.node || edge.target == n.node) {
							var otherNode:NodeSprite = edge.other(n.node);
							var contained:Boolean = false;
							var otherNodeAsNode:Node = findNode(nodes,otherNode);
							if (otherNodeAsNode == null)
								continue;
													
							for each (var visitedNode:Node in nodesByLevel)
								if (visitedNode == otherNodeAsNode) {
									contained = true;
									break;
								}	
								
							if (!contained) {			
								n.neighbors.push(otherNodeAsNode);
								nodesByLevel.push(otherNodeAsNode);
							}
						}
					}
					i++;
				}
				root = findNextRoot(nodes);
			}
			for each(var node:Node in nodesByLevel)
				node.visited = false;
		}
		
		private function findNode(nodes:Array,n:NodeSprite):Node {
			for each(var node:Node in nodes)
				if (node.node == n)
					return node;
			return null;
		}
		
		private function computeWidth(node:Node):void {
			var w:Number = 100;
			node.width = w;
			var count:Number = 0;
			for each(var n:Node in node.neighbors)
				if (n.width==0) {
					count++;
					computeWidth(n);
					if (n.width > w)
						w = n.width;
				}
			if (count>2)
				w *= 2;	
			node.width = Math.min(w,400);
		}
		
		private function layout(node:Node,root:Boolean):void {
			node.visited = true;
			var nChildren:Number = 0;
			if (!root)
				nChildren++;
				
			for each(var n:Node in node.neighbors)
				if (!n.visited)
					nChildren++;
			
			var angle:Number = 2 * Math.PI / (nChildren);
			var currentAngle:Number = (node.parentDirection + angle) % (2 * Math.PI);
			var additionalSpace:Number = 0;
			
			if (node.neighbors.length > 30)
				additionalSpace = 100;
			if (node.neighbors.length > 45)
				additionalSpace = 200;
			if (node.neighbors.length > 90)
				additionalSpace = 300;
				
			for each(n in node.neighbors) 
				if (!n.visited) {					
					n.x = node.x + (n.width+additionalSpace) * Math.cos(currentAngle);
					n.y = node.y + (n.width + additionalSpace) * Math.sin(currentAngle);
					_t.$(n.node).x = n.x;
					_t.$(n.node).y = n.y;
					n.parentDirection = (currentAngle + Math.PI) % (2 * Math.PI);
					currentAngle = (currentAngle + angle) % (2 * Math.PI);
					layout(n,false);
				}			
		}
	}	
}

class Node {
	import flare.vis.data.NodeSprite;
	
		public var x:Number;
		public var y:Number;
		public var node:NodeSprite;
		public var neighbors:Array = new Array();
		public var width:Number = 0;
		public var parentDirection:Number = 0.0;
		public var visited:Boolean = false;
		public function Node(node:NodeSprite) {
			this.node = node;
		}
	}
