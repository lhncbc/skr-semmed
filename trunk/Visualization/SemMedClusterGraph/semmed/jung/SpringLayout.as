package semmed.jung
{
	import flare.vis.data.Data;
	import flare.animate.Transitioner;
	import flare.vis.data.NodeSprite;
	import flare.vis.data.EdgeSprite;	
	import flare.vis.operator.layout.Layout;
//	import com.adobe.flex.extras.controls.springgraph.Graph;
	import semmed.GraphVisualizer;
	

	public class SpringLayout extends Layout
	{
		private var data:Data;				
		private var key:Object;	
		private var lengthFunction:UnitLengthFunction;
		
	    private var stretch:Number = 0.70;
	    private var repulsionRange:int = 100;
    	private var forceMultiplier:Number = 1.0 / 3.0;
		
		private var nonMoved:Number = 0;

		private var graph:GraphVisualizer
		
		private const UNITLENGTHFUNCTION:UnitLengthFunction = new UnitLengthFunction(30);	
				
		public function SpringLayout(g:Data, gv:GraphVisualizer) {
	        data = g;
			this.graph = gv;
	        lengthFunction = UNITLENGTHFUNCTION;
	    }

		
		private function initializeLocal():void {
	        for each (var e:EdgeSprite in data.edges){
	            var sed:SpringEdgeData = getEdgeData(e);
	            if (sed == null) {
	                sed = new SpringEdgeData(e);
	                e.data.extra = sed;
	            }
	            calcEdgeLength(sed, lengthFunction);
	        }
	 	}
	 	
	    protected function initializeLocalVertex(n:NodeSprite):void {
        	var vud:SpringVertexData  = getVertexData(n);
        	if (vud == null) {
            	vud = new SpringVertexData();
	            n.data.extra = vud;
        	}
     	}
     	
     	protected function calcEdgeLength(sed:SpringEdgeData, f:UnitLengthFunction):void {
        	sed.length = f.getLength(sed.e);
	    }
	    
		public override function operate(t:Transitioner=null):void{
			//_t = (t!=null ? t : Transitioner.DEFAULT);
			
			//++_gen; // update generation counter
			
			// populate simulation
			data = visualization.data;
			advancePositions();
		}
		       
	    //this is the main thing
	    public function advancePositions():void {
	        for each(var v:NodeSprite in data.nodes){
	            var svd:SpringVertexData = getVertexData(v);
	            if (svd == null) {
	                continue;
	            }
	            svd.dx /= 4;
	            svd.dy /= 4;
	            svd.edgedx = svd.edgedy = 0;
	            svd.repulsiondx = svd.repulsiondy = 0;
	        }
	        relaxEdges();
    	    calculateRepulsion();
        	moveNodes();
	    }
	    
	    protected function relaxEdges():void {

	        for each(var e:EdgeSprite in data.edges) {

	            var v1:NodeSprite = e.source;
	            var v2:NodeSprite = e.target;

				var p1:Object = {x:v1.x,y:v1.y};
				var p2:Object = {x:v2.x,y:v2.y};
				
	            if(p1 == null || p2 == null) 
	            	continue;

	           	var vx:Number = p1.x - p2.x;
    	        var vy:Number = p1.y - p2.y;
        	    var len:Number = Math.sqrt(vx * vx + vy * vy);
            
            	var sed:SpringEdgeData = getEdgeData(e);
	            if (sed == null) {
    	            continue;
        	    }
            	var desiredLen:Number = sed.length;

	            // round from zero, if needed [zero would be Bad.].
    	        len = (len == 0) ? .0001 : len;

	            var f:Number = forceMultiplier * (desiredLen - len) / len;

	            f = f * Math.pow(stretch, (v1.degree + v2.degree - 2));

	            // the actual movement distance 'dx' is the force multiplied by the
    	        // distance to go.
        	    var dx:Number = f * vx;
	            var dy:Number = f * vy;
	            var v1D:SpringVertexData, v2D:SpringVertexData;
	            v1D = getVertexData(v1);
    	        v2D = getVertexData(v2);

        	    sed.f = f;

            	v1D.edgedx += dx;
	            v1D.edgedy += dy;
    	        v2D.edgedx += -dx;
        	    v2D.edgedy += -dy;
	        }
	    }	
	    
	    protected function calculateRepulsion():void {
     
     	   for each(var v:NodeSprite in data.nodes) {
	            //if (isLocked(v)) continue;

	            var svd:SpringVertexData = getVertexData(v);
	            if(svd == null) continue;
    	        var dx:Number = 0.0;
    	        var dy:Number = 0.0;

	            for each(var v2:NodeSprite in data.nodes) {
	                if (v == v2) continue;
    	            var p:Object  = getLocation(v);
        	        var p2:Object = getLocation(v2);
	                if(p == null || p2 == null) continue;
    	            var vx:Number = p.x - p2.x;
    	            var vy:Number = p.y - p2.y;
            	    var distance:Number = vx * vx + vy * vy;
                	if (distance == 0) {
	                    dx += Math.random();
    	                dy += Math.random();
        	        } else if (distance < repulsionRange * repulsionRange) {
            	        var factor:Number = 1.0;
                	    dx += factor * vx / Math.pow(distance, 2);
                    	dy += factor * vy / Math.pow(distance, 2);
	                }
    	        }
            	var dlen:Number = dx * dx + dy * dy;
	            if (dlen > 0) {
    	            dlen = Math.sqrt(dlen) / 2;
        	        svd.repulsiondx += dx / dlen;
            	    svd.repulsiondy += dy / dlen;
	            }
    	    }
	    }
	    
	    protected function moveNodes():void {

			var moved:Boolean = false;
	      	var T:Number = 3.0;
	      	var T2:Number = 30;
            for each (var v:NodeSprite in data.nodes) {
	            //if (isLocked(v)) continue;
                var vd:SpringVertexData = getVertexData(v);
                if(vd == null) 
                	continue;
				var xyd:Object = {x:v.x,y:v.y};
                    
                vd.dx += vd.repulsiondx + vd.edgedx;
                vd.dy += vd.repulsiondy + vd.edgedy;
                    
                // keeps nodes from moving any faster than 5 per time unit
                xyd.x+=Math.max(-5, Math.min(5, vd.dx));
                xyd.y+=Math.max(-5, Math.min(5, vd.dy));
                    
                //var width:int = getCurrentSize().width;
                //var height:int = getCurrentSize().height;
                    
   //             if (xyd.x < 0) {
     //           	xyd.x = 0;
       //         }// else if (xyd.x > width) {
                //    xyd.x = width;
                //}
         //       if (xyd.y < 0) {
	       //         xyd.y = 0;
             //   } //else if (xyd.y > height) {
                  //  xyd.y = height;
               // }
               	moved = moved || Math.abs(v.x-xyd.x)>T || Math.abs(v.y-xyd.y)>T;
                v.x = xyd.x;
                v.y = xyd.y;
            }
            if (!moved){
            	nonMoved++;
            	if (nonMoved>T2){
	            	graph.stop();
	            	nonMoved = 0;	
	            }
            }else{
            	nonMoved = 0;
            }
        }
                
	    private function getVertexData(v:NodeSprite):SpringVertexData {
	        return v.data.extra as SpringVertexData;
	    }
	
	    private function getEdgeData(e:EdgeSprite):SpringEdgeData {
			return e.data.extra as SpringEdgeData;
		}
		
		private function getLocation(n:NodeSprite):Object{
			return {x:n.x,y:n.y};
		}
		
		public function update(data:Data):void{
			this.data = data;
			for each (var n:NodeSprite in data.nodes)
				initializeLocalVertex(n);
			initializeLocal();			
		}
	}
}