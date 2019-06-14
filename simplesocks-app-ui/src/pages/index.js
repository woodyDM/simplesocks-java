
import React from 'react';
import ajax from '../utils/request';
import { Descriptions, Badge } from 'antd';
export default class GeneralPage extends React.Component{


    state={}

    componentDidMount(){
        ajax.getEx(ajax.api.info).then(res=>{
           this.setState({data:res.data});
        });
    }
    



    render(){
        if(this.state.data){
            const data = this.state.data;
            return (
                <div>
                    <Descriptions title="概况" bordered column={2}>
                        <Descriptions.Item label="计算机" span={2}>{data.operatingSystemName}</Descriptions.Item>
                        <Descriptions.Item label="空闲物理内存">{data.freePhysicalMemory}</Descriptions.Item>
                        <Descriptions.Item label="总共物理内存">{data.totalPhysicalMemory}</Descriptions.Item>
                        <Descriptions.Item label="程序版本" span={2}>{data.version}</Descriptions.Item>
                        <Descriptions.Item label="初始启动时间">{data.startTime}</Descriptions.Item>
                        <Descriptions.Item label="活动的用户线程数">{data.totalActiveThread}</Descriptions.Item>
                        <Descriptions.Item label="可使用的最大堆内存">{data.vmMax}</Descriptions.Item>
                        <Descriptions.Item label="当前总堆内存">{data.vmTotal}</Descriptions.Item>
                        <Descriptions.Item label="当前使用堆内存">{data.vmUsed}</Descriptions.Item>
                        <Descriptions.Item label="当前剩余堆内存">{data.vmFree}</Descriptions.Item>
                    </Descriptions>
                    
                </div>
            );
        }else{
            return(<div> </div>)
        }

        
    }
}
