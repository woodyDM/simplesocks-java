
// ref: https://umijs.org/config/
export default {
  treeShaking: true,
  plugins: [
    // ref: https://umijs.org/plugin/umi-plugin-react.html
    ['umi-plugin-react', {
      antd: true,
      dva: false,
      dynamicImport: false,
      title: 'simplesocks-configuration',
      dll: false,
      
      routes: {
        exclude: [
          /components\//,
        ],
      },
    }],
  ],
  hash:true,
  proxy:{
    "/api":{
      "target":"http://localhost:10590/api/",
      "changeOrigin":true,
      "pathRewrite":{"^/api":""},
    }
  }
}
