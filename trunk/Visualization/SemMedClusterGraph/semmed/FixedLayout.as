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

	public class FixedLayout extends Layout{


		// private var _t:Transitioner;
		protected var _t:Transitioner;

		public function FixedlLayout() {

		}

		public override function operate(t:Transitioner=null):void
		{
			_t = (t!=null ? t : Transitioner.DEFAULT);

		}

	}
}

