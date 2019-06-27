package semmed.jung
{
	import flare.vis.data.EdgeSprite;
	
	public class SpringEdgeData
	{
		public var f:Number = 0.0;
		public var length:Number = 0.0;		
		public var e:EdgeSprite;
		
		public function SpringEdgeData(e:EdgeSprite){
			this.e = e;
		}
	}
}