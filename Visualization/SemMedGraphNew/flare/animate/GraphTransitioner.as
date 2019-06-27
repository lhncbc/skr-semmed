/**
* ...
* @author Default
* @version 0.1
*/

package flare.animate {
	import semmed.GraphVisualizer;

	public class GraphTransitioner extends Transitioner{
		
		private var graph:GraphVisualizer;
		
		public function GraphTransitioner(graph:GraphVisualizer, duration:Number) {
			super(duration);
			this.graph = graph;
		}
		
		protected override function end():void{
			super.end();
			graph.redraw();
		}
	}
	
}
