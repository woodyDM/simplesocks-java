
import React from 'react';
import {Table,Input,Button,Icon,Modal} from 'antd';
import ajax from '../utils/request';
import Highlighter from 'react-highlight-words';

export default class DomainList extends React.Component{

    constructor(props){
        super(props);
        this.state={
            searchText:"",
            dataSet:[],
            diagShow:false,
            newDomain:"",
        }

    }

    getColumnSearchProps = dataIndex=>({
        filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters }) => (
            <div style={{ padding: 8 }}>
              <Input
                ref={node => {
                  this.searchInput = node;
                }}
                placeholder={`Search ${dataIndex}`}
                value={selectedKeys[0]}
                onChange={e => setSelectedKeys(e.target.value ? [e.target.value] : [])}
                onPressEnter={() => this.handleSearch(selectedKeys, confirm)}
                style={{ width: 188, marginBottom: 8, display: 'block' }}
              />
              <Button
                type="primary"
                onClick={() => this.handleSearch(selectedKeys, confirm)}
                icon="search"
                size="small"
                style={{ width: 90, marginRight: 8 }}
              >
                搜索
              </Button>
              <Button onClick={() => this.handleReset(clearFilters)} size="small" style={{ width: 90 }}>
                重置
              </Button>
            </div>
          ),
          filterIcon: filtered => (
            <Icon type="search" style={{ color: filtered ? '#1890ff' : undefined }} />
          ),
          onFilter: (value, record) =>
            record[dataIndex]
              .toString()
              .toLowerCase()
              .includes(value.toLowerCase()),
          onFilterDropdownVisibleChange: visible => {
            if (visible) {
              setTimeout(() => this.searchInput.select());
            }
          },
          render: text => (
            <Highlighter
              highlightStyle={{ backgroundColor: '#ffc069', padding: 0 }}
              searchWords={[this.state.searchText]}
              autoEscape
              textToHighlight={text.toString()}
            />
          ),
        });

    

    handleSearch = (selectedKeys, confirm) => {
        confirm();
        this.setState({ searchText: selectedKeys[0] });
    };
    handleReset = clearFilters => {
        clearFilters();
        this.setState({ searchText: '' });
    };

    componentDidMount(){
        this.refresh();
    }

    refresh=()=>{
        const type = this.props.type;
        const url = ajax.api.domain;
        ajax.getEx(url,{type:type}).then(res=>{
            const dataSet = res.data.map(d=>{return({domain:d,enable:true})});
            if(type==='white'){
                dataSet.push({domain:"localhost",enable:false});
                dataSet.push({domain:"127.0.0.1",enable:false});
            }
            for(let i = 0;i<dataSet.length;i++){
                dataSet[i].key = i+1;
            }
            this.setState({dataSet:dataSet});
        })
        
    }

    onAddNewDomain=()=>{
        this.setState({newDomain:"",diagShow:true});
    }

    newDomainChange=(e)=>{
        const value = e.target.value;
        this.setState({newDomain:value });
    }

    handleDiagCancel= ()=>{
        this.setState({newDomain:"",diagShow:false});
    }

    handleDiagOk=()=>{
        const type = this.props.type;
        let domain = this.state.newDomain;
        if(domain && domain.trim()){
            domain = domain.trim();
            ajax.postEx(ajax.api.domain, {type:type,domain:domain}).then(ref=>{
                this.setState({newDomain:"",diagShow:false},()=>{
                    this.refresh();
                });
            })
        }else{
            this.setState({newDomain:"",diagShow:false});
        }
    }

    deleteDomain = (domain)=>{
        const type = this.props.type;
        const url = ajax.api.domainDelete;
        ajax.postEx(url,{type:type,domain:domain}).then(res=>{
            this.refresh();
        })
    }
    
    render(){
        const columns = [
            {
              title: '序号',
              dataIndex: 'key',
              key: 'key',
              width: '10%',
            },
            {
              title: '域名',
              key: 'domain',
              dataIndex: 'domain',
              width: '60%',
              ...this.getColumnSearchProps('domain'),
            },
            {
              title: '操作',
              width:"20%",
              style:"height:20px",
              render:(text,record)=>(record.enable?
              <a 
              href="javascript:;"
              onClick={()=> this.deleteDomain(record.domain)}
              style={{ marginRight: 8 }}>删除</a>
              :"")
            },
          ];


        return(<div>
                <Modal
                    title="新增域名"
                    visible={this.state.diagShow}
                    onOk={this.handleDiagOk}
                    onCancel={this.handleDiagCancel}
                    >
                    <Input width='60%' 
                    value={this.state.newDomain}
                    onChange={this.newDomainChange} 
                    placeholder='请输入域名'/>
                </Modal>
                <div style={{margin:"10px 20px"}}>
                    <Button type='primary' onClick={this.onAddNewDomain}>新增</Button>
                    
                </div>
                
                <Table 
                    pagination = {false}
                    columns={columns} 
                    dataSource={this.state.dataSet} 
                />
            </div>);
    }
}