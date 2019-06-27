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
	import flare.vis.data.render.IRenderer;

	public class EdgeRenderer implements IRenderer
	{
		private static var _instance:EdgeRenderer = new EdgeRenderer();
		
		private static var pattern:Array = [20, 5];

		public static function get instance():EdgeRenderer { return _instance; }

		
		public static function manualRender(d:DataSprite) :void{
			var e:EdgeSprite = d as EdgeSprite;
			if (e == null)  
				return;
				
			var s:NodeSprite = e.source;
			var t:NodeSprite = e.target;
			var g:Graphics = e.graphics;
			
			g.clear(); // clear it out
			
			if (s==null || t==null) 
				return; 
			
			if (!s.visible || !t.visible)
				return;
				
			if (s == t)
				return;
				
			var x:Number,y:Number,x2:Number,y2:Number;
			//g.moveTo(e.x1, e.y1);
			
			//x = e.x1;
			//y = e.y1;
			x = s.x;
			y = s.y;
			x2 = t.x;
			y2 = t.y;
			if (d.data.glow)
				drawPointyArrow(e,g,x,y,x2,y2,7,true);			
			else
				//drawPointyArrow(e,g,x,y,e.x2,e.y2,7,false);			
				drawPointyArrow(e,g,x,y,x2,y2,7,false);			
			//drawRoundArrow(e,g,x,y,e.x2,e.y2,7);	
			
		}
		
		public function render(d:DataSprite):void
		{
			manualRender(d);
			//var e:EdgeSprite = d as EdgeSprite;
			//if (e == null)  
			//	return;
			//	
			//var s:NodeSprite = e.source;
			//var t:NodeSprite = e.target;
			//var g:Graphics = e.graphics;
			
			//if (s==null || t==null) { 
			//	g.clear(); 
			//	return; 
			//}
			
			//var ctrls:Array = e.points as Array;
				
			//g.clear(); // clear it out
			//setLineStyle(e, g); // set the line style
			
			//if (e.shape == Shapes.BEZIER && ctrls != null && ctrls.length > 1) {
			//	if (ctrls.length < 4){
			//		g.moveTo(e.x1, e.y1);
			//		g.curveTo(ctrls[0], ctrls[1], e.x2, e.y2);
			//	}else{
			//		GraphicsUtil.drawCubic(g, e.x1, e.y1, ctrls[0], ctrls[1],
			//							   ctrls[2], ctrls[3], e.x2, e.y2);
			//	}
			//} else if (e.shape == Shapes.CARDINAL) {
			//	GraphicsUtil.drawCardinal2(g, e.x1, e.y1, ctrls, e.x2, e.y2);
			//} else {
			//	var x:Number,y:Number;
			//	g.moveTo(e.x1, e.y1);
			//	if (ctrls != null) {
			//		for (var i:uint=0; i<ctrls.length; i+=2)
			//			g.lineTo(ctrls[i], ctrls[i+1]);
			//	}				
			//	if (ctrls!=null){
			//		x = ctrls[ctrls.length-2];
			//		y = ctrls[ctrls.length-1];					
			//	}else{
			//		x = e.x1;
			//		y = e.y1;
			//	}		
																	
				//if (d.data.glow)
				//	drawPointyArrow(e,g,x,y,e.x2,e.y2,7,true);			
				//else
				//	drawPointyArrow(e,g,x,y,e.x2,e.y2,7,false);			
				//drawRoundArrow(e,g,x,y,e.x2,e.y2,7);	
			//}
		}
		
		protected function setLineStyle(e:EdgeSprite, g:Graphics):void
		{
			var lineAlpha:Number = e.lineAlpha;
			if (lineAlpha == 0) return;
			
			var sm:String = e.props.scaleMode;
			if (sm == null) sm = "normal";
			g.lineStyle(e.lineWidth, e.lineColor, lineAlpha, e.props.pixelHinting, sm);
		}		
		
		private static function drawPointyArrow(e:EdgeSprite,g:Graphics,x0:Number,y0:Number,x1:Number,y1:Number,l:Number,glow:Boolean):void{
			var a:Number,b:Number,c:Number;
			
			var t:Number = l/Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));
						
			var rPoint:Object = {x:x1+(x0-x1)*t,y:y1+(y0-y1)*t};									
			var rVector:Object = {x:rPoint.x-x1,y:rPoint.y-y1};
			
			var k:Number = Math.SQRT2/2.0;			
			var r1:Object = {x:(k*rVector.x-k*rVector.y)*0.75,y:(k*rVector.x/2+k*rVector.y/2)*0.75};			
			var r2:Object = {x:(k*rVector.x+k*rVector.y)*0.75,y:(-k*rVector.x/2+k*rVector.y/2)*0.75};
			
			//setLineStyle(e,g);
			if (glow){
				g.lineStyle(e.lineWidth+6,e.fillColor,0.4);
				g.moveTo(x0,y0);
				g.lineTo(rPoint.x,rPoint.y);
			}
			
			if ((e.data.predicate as String).search("NEG_") == 0) {
				drawDashedLine(g, x0, y0, rPoint.x, rPoint.y, e);
			}else{
				g.lineStyle(e.lineWidth,e.fillColor,e.lineAlpha);
				g.moveTo(x0,y0);
				g.lineTo(rPoint.x, rPoint.y);
			}
						
			g.moveTo(rPoint.x,rPoint.y);
			g.beginFill(e.fillColor, e.lineAlpha);
			g.lineTo(rPoint.x+r1.x,rPoint.y+r1.y);
			g.lineTo(rPoint.x+r2.x,rPoint.y+r2.y);
			g.lineTo(rPoint.x,rPoint.y);
			g.endFill();						
		}
		
		private static function drawRoundArrow(e:EdgeSprite,g:Graphics,x0:Number,y0:Number,x1:Number,y1:Number,l:Number):void{
			var a:Number,b:Number,c:Number;
			var rMinor:Number = 3.0;
			
			var t:Number = (l+rMinor)/Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1));

			var r:Object = {x:x1+(x0-x1)*t,y:y1+(y0-y1)*t};

			g.lineStyle(e.lineWidth,e.fillColor,e.lineAlpha);
			g.moveTo(x0,y0);
			g.lineTo(r.x,r.y);
			
			g.moveTo(r.x,r.y);
			g.beginFill(e.fillColor, e.lineAlpha);
            g.lineStyle(0.5, 0x000000);
			g.drawCircle(r.x, r.y, rMinor);
			g.endFill();			
		}
		
		private static function _drawDashedLine(target:Graphics,pattern:Array,
												drawingState:DashStruct,
												x0:Number,y0:Number,x1:Number,y1:Number,e:EdgeSprite):void
		{			
			var dX:Number = x1 - x0;
			var dY:Number = y1 - y0;
			var len:Number = Math.sqrt(dX*dX + dY*dY);
			dX /= len;
			dY /= len;
			var tMax:Number = len;
			
			
			var t:Number = -drawingState.offset;
			var bDrawing:Boolean = drawingState.drawing;
			var patternIndex:int = drawingState.patternIndex;
			var styleInited:Boolean = drawingState.styleInited;
			while(t < tMax)
			{
				t += pattern[patternIndex];
				if(t < 0)
				{
					var x:int = 5;
				}
				if(t >= tMax)
				{
					drawingState.offset = pattern[patternIndex] - (t - tMax);
					drawingState.patternIndex = patternIndex;
					drawingState.drawing = bDrawing;
					drawingState.styleInited = true;
					t = tMax;
				}
				
				if(styleInited == false)
				{
					if(bDrawing)
						target.lineStyle(e.lineWidth,e.fillColor,e.lineAlpha);
					else
						target.lineStyle(0,0,0);
				}
				else
				{
					styleInited = false;
				}
					
				target.lineTo(x0 + t*dX,y0 + t*dY);

				bDrawing = !bDrawing;
				patternIndex = (patternIndex + 1) % pattern.length;
			}
		}

				
		public static function drawDashedLine(target:Graphics,x0:Number,y0:Number,x1:Number,y1:Number,e:EdgeSprite):void
		{
			target.moveTo(x0,y0);
			var struct:DashStruct = new DashStruct();							
			_drawDashedLine(target,pattern,struct,x0,y0,x1,y1,e);
		}
	}
}

class DashStruct
	{
		public function init():void
		{
			drawing = true;
			patternIndex = 0;
			offset = 0;
		}
		public var drawing:Boolean = true;
		public var patternIndex:int = 0;
		public var offset:Number = 0;	
		public var styleInited:Boolean = false;
	}