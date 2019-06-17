import axios from 'axios';
import { message } from 'antd';
import api from './api';

const codeMessage = {
  200: '服务器成功返回请求的数据。',
  201: '新建或修改数据成功。',
  202: '一个请求已经进入后台排队（异步任务）。',
  204: '删除数据成功。',
  400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  401: '用户没有权限（令牌、用户名、密码错误）。',
  403: '用户得到授权，但是访问是被禁止的。',
  404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  406: '请求的格式不可得。',
  410: '请求的资源被永久删除，且不会再得到的。',
  422: '当创建一个对象时，发生一个验证错误。',
  500: '服务器发生错误，请检查服务器。',
  502: '网关错误。',
  503: '服务不可用，服务器暂时过载或维护。',
  504: '网关超时。',
};

// 全局默认配置
axios.defaults.timeout = 200000;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8;Accept-Language:zh-CN,zh;q=0.8';
 

// 返回拦截器
axios.interceptors.response.use(config => {
  return config;
}, (error) => {
  if (error && error.response) {
    const code = error.response.status;
    console.log(code);
    const msg = codeMessage[code];
    if(msg){
      message.error(msg);
    }else{
      message.error("发生未知错误!");
    }

  } else if (JSON.stringify(error).indexOf('timeout') !== -1) {
    message.error('连接超时,请刷新试试');
  }
});

 

/**
 * 带有默认处理的方法 TODO res判断
 * @param {*} url 路径
 * @param {*} params 参数
 * @param {*} methodFunction 函数
 */
const doActionEx = (url, params, methodFunction)=>{
  return new Promise((resolve, reject) => {
    methodFunction(url, params,{timeout: 1000 * 10  })
      .then(res => {
        if (res && res.data.success) {
          resolve(res.data);
        }else if(res){
          message.error(res.data.msg);
          reject(res);
        }
      })
      .catch(err => {
        reject(err);
      });
  });
}

const getEx = (url, params) => {
  return doActionEx(url, params, (url, params)=>axios.get(url, {params:params}));
};
const postEx = (url, params) => {
  return doActionEx(url, params, axios.post);
};
const deleteEx = (url, params) => {
  return doActionEx(url, params, axios.delete);
};




export default {api, getEx , postEx ,deleteEx};