
import React from 'react';
import {Card,Select,Button,notification} from 'antd';
import ajax from '../../utils/request';
import styles from './index.less';
const {Option} = Select;



export default class GitPage extends React.Component{

    state={hasProxy:false,setting:"SET"}

    componentDidMount(){
        this.refresh();
    }

    refresh = ()=>{
        ajax.getEx(ajax.api.gitInfo).then(res=>{
        
            this.setState({info:res.data,hasProxy:res.data.hasProxyData});
        })
    }

    onSettingOk=()=>{
        const setting = this.state.setting;
        ajax.postEx(ajax.api.gitSetting,{type:setting}).then(res=>{
            notification['success']({message:'success', description:"设置成功"})
            this.refresh();
        })
    }
    
    handleSettingChange = (v)=>{
        this.setState({setting:v});
    }

    render(){
        const info = this.state.info;
       
        if(info){
            const port = this.state.info.localPort;
            let tips={
                SET:(<div className={styles.tips}>
                        <p>git config --global http.proxy socks5://127.0.0.1:{port}</p>
                        <p>git config --global https.proxy socks5://127.0.0.1:{port}</p>
                    </div>),
                SET_AND_GLOBAL_PROXY:(<div className={styles.tips}>
                    <p>git config --global http.proxy socks5://127.0.0.1:{port}</p>
                    <p>git config --global https.proxy socks5://127.0.0.1:{port}</p>
                    <p>set globalProxy = true</p>
                    </div>),
                RESET:(<div className={styles.tips}>
                    <p>git config --global --unset http.proxy</p>
                    <p>git config --global --unset https.proxy</p>
                    </div>),
                RESET_AND_NO_GLOBAL_PROXY:(<div className={styles.tips}>
                    <p>git config --global --unset http.proxy</p>
                    <p>git config --global --unset https.proxy</p>
                    <p>set globalProxy = false</p></div>)
            }
            return (
                <div>
                     <Card title="Git代理配置">
                         
                        <Card type="inner" title="当前Git代理信息" extra={false}>
                        <p>SimpleSocks代理模式：{this.state.info && this.state.info.isGlobalMode ?"全局模式":"PAC模式"}</p>
                        <p>Git全局代理地址：{  this.state.info.httpProxy? this.state.info.httpProxy:"无" }</p>
            
                        </Card>
                        <Card
                        style={{ marginTop: 16 }}
                        type="inner"
                        title="配置"
                        extra={false}
                        >
                        选择模式： 
                        <Select defaultValue="SET" style={{ width: '300px' }} onChange={this.handleSettingChange}>
                            <Option value="SET">仅启用代理</Option>
                            <Option value="SET_AND_GLOBAL_PROXY">启用代理-全局模式</Option>
                            <Option value="RESET">仅取消代理</Option>
                            <Option value="RESET_AND_NO_GLOBAL_PROXY">取消代理-PAC模式</Option>
                        </Select>
                        <Button 
                        style={{marginLeft:'25px'}}
                        type='primary' 
                        onClick={this.onSettingOk}>确定</Button>
                        <div style={{marginTop:'20px'}}>
                        <p>相当于执行：</p>
                        {tips[this.state.setting]}
                        </div>
                        
                        </Card>
                     </Card>
                </div>
            );
        }else{
            return(<div></div>)
        }
        
    }
}