
import React from 'react';
import ajax from '../../utils/request';
import router from 'umi/router';

import {
    notification,
    Form,
    Input,
    InputNumber,
    Tooltip,
    Icon,
    Cascader,
    Select,
    Row,
    Col,
    Checkbox,
    Button,
    AutoComplete,
  } from 'antd';
 
  
const { Option } = Select;


class SettingPage extends React.Component{

    fields = ['auth','configServerPort','encryptType','globalProxy','localPort','remoteHost','remotePort'];

    validateLocalPort = (rule,value,callback)=>{
      this.validatePort(rule,value,callback,"configServerPort" )
    }

    validateConfigPort = (rule,value,callback)=>{
      this.validatePort(rule,value,callback,"localPort" )
    }

    validatePort = (rule,value,callback, field)=>{
        const form = this.props.form;
        const localPort = form.getFieldValue("localPort");
        const configPort = form.getFieldValue("configServerPort");
        if(localPort==configPort){
          callback("代理端口不能和配置端口相同");
        }else{
          const isVal = form.getFieldError(field);
          if(isVal){
            form.validateFields([field]);
          }
          callback();
        }
    }

    onSubmit = ()=>{
      const form = this.props.form;
      form.validateFields();
      const err = form.getFieldsError();
      let hasErr = false;
      for(let i in err){
        if(err[i]!==undefined){
          hasErr = true;
          break;
        }
      }
      if(hasErr) return;
      const values = form.getFieldsValue(this.fields);
      ajax.postEx(ajax.api.doSetting, values).then(res=>{
        notification['success']({title:'success', description:"设置成功"})
        router.push({pathname:"/"});
      })
    }

    componentDidMount(){
      const form = this.props.form;
      ajax.getEx(ajax.api.setting).then(res=>{
        let data = res.data;
        let d = {};
        for(let i in this.fields){
          d[this.fields[i]] = data[this.fields[i]];
        }
        d.globalProxy = (d.globalProxy+"");
        form.setFieldsValue(d);
      })
    }

    render(){
        const { getFieldDecorator } = this.props.form;
        const formItemLayout = {
            labelCol: {
                xs: { span: 8 },
                sm: { span: 8 },
            },
            wrapperCol: {
                xs: { span: 8 },
                sm: { span: 8 },
            },
        }; 
        const tailFormItemLayout = {
            wrapperCol: {
              xs: {
                span: 16,
                offset: 0,
              },
              sm: {
                span: 16,
                offset: 8,
              },
            },
          };
       
      
        return (
        <Form {...formItemLayout} onSubmit={this.handleSubmit}>
        <Form.Item label="本地代理端口"   required={false}>
          {getFieldDecorator('localPort', {
            rules:[{required:true,message:"端口不能为空"},{validator:this.validateLocalPort}]
          } )(<InputNumber min={1} max={65535} />)}
        </Form.Item>
        <Form.Item label="本地配置端口"   required={false}>
          {getFieldDecorator('configServerPort',   {
            rules:[{required:true,message:"端口不能为空"},{validator:this.validateConfigPort}]
          } )(<InputNumber min={1} max={65535} />)}
        </Form.Item>
        <Form.Item label="远程服务器地址" hasFeedback  required={false}>
          {getFieldDecorator('remoteHost', {
            rules: [{required:true,message:"远程服务器地址不能为空"}],
          })(<Input  />)}
        </Form.Item>
        <Form.Item label="远程服务器端口"  required={false}>
          {getFieldDecorator('remotePort', {
            rules:[{required:true,message:"端口不能为空"}]
          })(<InputNumber min={1} max={65535}/>)}
        </Form.Item>
        <Form.Item label="远程服务器密码" required={false}>
          {getFieldDecorator('auth', {
            rules: [
              {required:true,message:"密码不能为空"},
            ],
          })(<Input.Password />)}
        </Form.Item>
        <Form.Item label="加密方式" required={false}>
          {getFieldDecorator('encryptType', { rules: [
              {required:true,message:"加密方式不能为空"},
            ],})(
            <Select style={{width:'150px'}}>
                <Option value="aes-cbc">aes-cbc</Option>
                <Option value="aes-cfb">aes-cfb</Option>
                <Option value="caesar">caesar</Option>
            </Select>
          )}
        </Form.Item>
        <Form.Item label="全局代理"  required={false}>
          {getFieldDecorator('globalProxy', { rules: [
              {required:true,message:"代理模式不能为空"},
            ],})(
              <Select style={{width:'150px'}}>
                <Option value="true">是</Option>
                <Option value="false">否</Option>
              </Select>
          )}
        </Form.Item>
        <Form.Item {...tailFormItemLayout}>
          <Button type="primary" htmlType="submit" onClick={this.onSubmit}>
            确定
          </Button>
        </Form.Item>
      </Form>
        );
    }
}
const WrappedSettingPage = Form.create({ name: 'setting' })(SettingPage);
export default WrappedSettingPage;