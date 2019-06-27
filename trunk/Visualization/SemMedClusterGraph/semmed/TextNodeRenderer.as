package semmed
{
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	import flash.display.Graphics;
	import flash.geom.Point;
	import flare.vis.util.graphics.GraphicsUtil;
	import flare.vis.util.graphics.Shapes;
	import flare.vis.data.render.NullRenderer;
	import flare.vis.data.render.ShapeRenderer;

	public class TextNodeRenderer extends ShapeRenderer
	{
		public var zfactor:Number;
		/** @inheritDoc */
		public function  setZoom(zoom:Number):void
		{
			zfactor = zoom;
		}
		public override function render(d:DataSprite):void
		{
			var lineAlpha:Number = d.lineAlpha;

			var fillAlpha:Number = d.fillAlpha;
			var size:Number = d.size * defaultSize;

			var g:Graphics = d.graphics;
			g.clear();
			if (fillAlpha > 0){
				//g.beginFill(d.fillColor, fillAlpha);
				g.beginFill(d.trueFillColor,d.trueFillAlpha);
			}
			if (lineAlpha > 0)
				g.lineStyle(d.lineWidth, d.trueLineColor, d.trueLineAlpha);

			switch (d.shape) {
				case Shapes.BLOCK:
					g.drawRect(d.u-d.x, d.v-d.y, d.w, d.h);
					break;
				case Shapes.POLYGON:
					if (d.points!=null) GraphicsUtil.drawPolygon(g, d.points);
					break;
				case Shapes.POLYBLOB:
					if (d.points!=null) GraphicsUtil.drawPolygon(g, d.points);
					break;
				case Shapes.VERTICAL_BAR:
					g.drawRect(-size/2, -d.h, size, d.h);
					break;
				case Shapes.HORIZONTAL_BAR:
					g.drawRect(-d.w, -size/2, d.w, size);
					break;
				case Shapes.WEDGE:
					GraphicsUtil.drawWedge(g, -d.x, -d.y,
								  d.h, d.v, d.u, d.u+d.w);
					break;
				default:
					shapePalette.getShape(d.shape)(g, size/zfactor);
			}

			if (fillAlpha > 0) g.endFill();
		}
	}
}