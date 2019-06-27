package semmed.jung
{
	import flare.vis.data.EdgeSprite;
	
	public class UnitLengthFunction
	{
		private var length:int;
		
		public function UnitLengthFunction(length:int) {
            this.length = length;
        }

        public function getLength(e:EdgeSprite):Number {
            return length;
        }
	}
}