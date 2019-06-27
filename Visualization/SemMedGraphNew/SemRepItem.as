package
{
    import mx.controls.Alert;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.core.Application;
    import com.adobe.flex.extras.controls.springgraph.Item;

    public class SemRepItem extends Item
    {
        [Bindable]
        public var name: String;
        
        [Bindable]
        public var longName: String;
                
        //[Bindable]
        //public var imageUrl: String;
                
        //private var similarProducts: XMLList;
        //private var createSimilarsASAP: Boolean = false;
        
        public function SemRepItem(id:String, name: String, longName: String) {
            super(name);
            this.name = name;
            this.id = id;
            this.longName = longName;
            //AmazonService.getItemInfo(itemId, this);
        }

        public function getItemInfoFault(event:FaultEvent):void {
            Alert.show("getItemInfoFault " + event.toString());
        }
    }
}