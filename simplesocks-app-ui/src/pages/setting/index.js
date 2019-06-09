
import React from 'react';
import {
    Form,
    Input,
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

    validatePort = (rule,value,callback)=>{
        callback("lalala"+value);
    }

    validateText = (rule,value,callback)=>{
        callback("lalala"+value);
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
        <Form.Item label="本地代理端口">
          {getFieldDecorator('localport', {
            rules: [
              {
                validator:this.validatePort,
              }
            ],
          })(<Input />)}
        </Form.Item>
        <Form.Item label="本地配置端口" hasFeedback>
          {getFieldDecorator('password', {
            rules: [
              {
                validator: this.validatePort,
              },
            ],
          })(<Input />)}
        </Form.Item>
        <Form.Item label="远程服务器地址" hasFeedback>
          {getFieldDecorator('remoteHost', {
            rules: [
                {
                    validator: this.validateText,
                  },
            ],
          })(<Input  />)}
        </Form.Item>
        <Form.Item
          label="远程服务器端口"
        >
          {getFieldDecorator('remotePort', {
            rules: [{
                validator: this.validatePort,
              }],
          })(<Input />)}
        </Form.Item>
        <Form.Item label="远程服务器密码">
          {getFieldDecorator('auth', {
             
            rules: [
                {
                    validator: this.validateText,
                  },
            ],
          })(<Input.Password />)}
        </Form.Item>
        <Form.Item label="加密方式">
          {getFieldDecorator('encType', {
            rules: [ ],
          })(
            <Select defaultValue="aes-cbc" style={{width:'150px'}}>
                <Option value="aes-cbc">aes-cbc</Option>
                <Option value="aes-cfb">aes-cfb</Option>
                <Option value="caesar">caesar</Option>
            </Select>
          )}
        </Form.Item>
        <Form.Item label="全局代理">
          {getFieldDecorator('globalProxy', {
            rules: [],
          })(
            <Select defaultValue="yes" style={{width:'150px'}}    >
                <Option value="yes">是</Option>
                <Option value="no">否</Option>
                
            </Select>
          )}
        </Form.Item>
        <Form.Item {...tailFormItemLayout}>
          <Button type="primary" htmlType="submit">
            确定
          </Button>
          
        </Form.Item>
      </Form>
        );
    }
}
const WrappedSettingPage = Form.create({ name: 'setting' })(SettingPage);
export default WrappedSettingPage;