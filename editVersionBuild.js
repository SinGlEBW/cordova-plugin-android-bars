const fs = require('fs');
const path = require('path');
const convert = require('xml-js');


const editVersionBuild = (version) => {
  let chunkVersion = version.split('.');

  let copyChunkVersion = [...chunkVersion];
  let firstValueVersion = chunkVersion[0];
  let middleValueVersion = chunkVersion[1];
  let endValueVersion = chunkVersion[2];

  let newVersion = [];
  if(Number(endValueVersion) >=  100){
    newVersion = [firstValueVersion, (+middleValueVersion + 1).toString(), (1).toString()];
    
  }else{
    newVersion = [firstValueVersion, middleValueVersion, (+endValueVersion + 1).toString()]
  }

  return newVersion.join('.');
}




const setVersionBuild = () => {

    let pathFilePackageJson = path.resolve(__dirname, 'package.json');

    fs.readFile(pathFilePackageJson, (err, data) => {
      if(!err){
        
        let packageJson = JSON.parse(data.toString());
        let version = editVersionBuild(packageJson.version);

        let pathFilePluginXML = path.resolve(__dirname, 'plugin.xml');
        let xml = fs.readFileSync(pathFilePluginXML, 'utf8');

        let XmlString = convert.xml2json(xml, {elementNameFn: (key, ref) => {
          if(ref.name === 'plugin'){
            ref.attributes.version = version
          }
          return key
        }});


        let updateVersionFilePluginXML = convert.json2xml(XmlString);

        let updateVersionPackageJson = JSON.stringify({...packageJson, version}, null, ' ');
        fs.writeFileSync(pathFilePackageJson, updateVersionPackageJson);
        fs.writeFileSync(pathFilePluginXML, updateVersionFilePluginXML);
        
      }
    })


    // editVersionFile(pathFilePackageJson);
   
    
    
    // editVersionFile(pathFilePluginXML);

}

setVersionBuild();


