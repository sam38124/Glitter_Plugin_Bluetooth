"use strict";
function onCreate() {
  glitter.runJsInterFace("Glitter_BLE_SetCallBack",{},function (response){
      switch (response.function){
          case "needGPS":
               console.log("需要開啟定位來掃描藍芽")
              break
          case "onConnectFalse":
              console.log("藍芽連線失敗")
              break
          case "onConnectSuccess":
              console.log("藍芽連線成功")
              break
          case "onConnecting":
              console.log("藍芽連線中")
              break
          case "onDisconnect":
              console.log("藍芽斷線")
              break
          case "requestPermission":
              console.log("權限不足"+JSON.stringify(response.data))
              break
          /**
           * readHEX,readBytes,readUTF
           * */
          case "rx":
              console.log("收到藍芽資料"+response.data.readHEX)
              break
          case "tx":
              console.log("傳送藍芽資料"+response.data.readHEX)
              break
          /**
           * device:{name,address}
           * advertise:{readHEX,readBytes,readUTF}
           * */
          case "scanBack":
              console.log("收到藍芽裝置"+response.device.name)
              break
      }
  })

   glitter.setHome('page/home.html','home',{})
}
