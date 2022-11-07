# cordova-plugin-zkFinger

`This is a simple unofficial plugin to integrate basic finger scanning functionality via the zkteco zk4500 to your android device`

Feel free to make contributions to the package.

### To install 
> ionic cordova plugin add cordova-plugin-zkfinger


### To scan a finger
`After the 'deviceready' event`
> cordova.plugins.zkFinger.scan(Function success_callback, Function error_callback)

e.g 

> 
    //@ Define the success callback
    const success = ( msg ) => {
        //@ do something
        <!-- msg contains the fingerprint data -->
    };

    //@ Define the error/alternative callback 
    const error     = err_msg => {
        //@ do something else e.g display the error message
    };

    //@ Scan
    cordova.plugins.zkFinger.scan( success, error);

    //! That's about all



### To save a template to the device
`After the 'deviceready' event`
> cordova.plugins.zkFinger.saveTemplate(String filepath, String fileContent, Function success_callback, Function error_callback)

e.g 

> 
    //@ Define the success callback
    const success = ( msg ) => {
        //@ do something
    };

    //@ Define the error/alternative callback 
    const error     = err_msg => {
        //@ do something else e.g display the error message
    };

    //@ write
    cordova.plugins.zkFinger.saveTemplate( "/storage/emulated/0/bixbyte_user_1.txt", "Finger Template Data" ,success, error);

    //! That's about all