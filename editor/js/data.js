var product_details = {
    wso2_complex_event_processor:{
        title:'Complex Event Processor',
        description:"Iidentify the most meaningful events and patterns from multiple data sources, analyze their " +
        "impacts, and act on them in real time. <br/>" +
        "suited for complex queries involving time windows, as well as patterns and sequences detection. <br/>" +
        "WSO2 CEP particularly well-suited to handle IoT scenarios. <br/>"
    },
    dashboard:{
        title:'Dash Board',
        description:"Digital dashboards allow managers to monitor the contribution of the various departments in their " +
        "organization.<br/> " +
        "To gauge exactly how well an organization is performing overall, digital dashboards allow you to capture and " +
        "report specific data points from each department within the organization," +
        " thus providing a 'snapshot' of performance." +
        " <ul> <li>Visual presentation of performance measures. </li> " +
        " <li> Ability to identify and correct negative trends. </li> " +
        " <li> Measure efficiencies/inefficiencies. </li> </ul>"
    },
    data_storage:{
        title:'Data Storage',
        description:"Data Storage is a persistance data store used to store data."
    },
    data_stream:{
        title:"Data Stream",
        description:"In Connection-oriented communication, a data stream is a sequence of digitally encoded coherent " +
        "signals (packets of data or data packets) used to transmit or receive information that is in the process of " +
        "being transmitted."
    },
    database:{
        title:'Database',
        description:"Database can be an any kind of modern database. <br />" +
        "Ex:- MySQL, MongoDB, H2"
    },
    wso2_api_manager:{
        title:'WSO2 API Manager',
        description:"WSO2 API Manager is a complete enterprise-class API management solution that combines easy," +
        " managed API access with full API governance and analysis.<br/>" +
        " The traffic manager featuring a dynamic throttling engine easily manages and scales API traffic while its" +
        " enhanced analytics provides greater insight into API usage, performance and anomalies.<br/>" +
        " It leverages proven components from the WSO2 platform to secure APIs and provides stronger governance across " +
        "APIs, services and applications with full API lifecycle visualization."
    },
    wso2_enterprise_service_bus:{
        title:'WSO2 Enterprise Service Bus(ESB)',
        description:"Can use as a service gateway, mediation engine across SAP, Salesforce, and as healthcare hub or " +
        "in IoT scenarios," +
        " made possible by variety of transports such as Apache Kafka and MQTT.<br/>" +
        " ESB mediates, enriches and transforms messages across a variety of systems, including legacy applications, " +
        "SaaS applications, as well as services and APIs.<br/>" +
        " WSO2 ESB offers intuitive and visually supported development tools that provide a smooth experience in " +
        "development environments and production deployments."
    },
    wso2_identity_server:{
        title:"WSO2 Identity Server",
        description:"Connects and manages multiple identities across applications, APIs, the cloud, mobile, and " +
        "Internet of Things devices, regardless of the standards on which they are based.<br/>" +
        " The multi-tenant WSO2 Identity Server can be deployed directly on servers or in the cloud," +
        " and has the ability to propagate identities across geographical and enterprise borders in a connected " +
        "business environment."
    },
    wso2_machine_learner:{
        title:"WSO2 Machine Learner",
        description:"WSO2 Machine Learner takes data one step further," +
        " pairing data gathering and analytics with predictive intelligence: this helps you understand not just " +
        "the present," +
        " but to predict scenarios and generate solutions for the future." +
        "<ul><li>Predicting business scenarios</li>" +
        "<li>Anomaly Detection</li>" +
        "<li>Predictive Maintenance</li></ul>"
    },
    user:{
        title:"User",
        description:"This component rempresent an end user of the system."
    },
    mobile_phone:{
        title:"Mobile Phone",
        description:"Mobile Devices:<br/>" +
        "   Examples:- Mobile Phones, Tabs, etc. "
    },
    web_page:{
        title:"Web Page",
        description:"Simple web page"
    },
    ml_model:{
        title:"ML Model",
        description:"<ul><li>Predicting business scenarios</li>" +
        "<li>Anomaly Detection</li>" +
        "<li>Predictive Maintenance</li>"
    },
    publisher:{
        title:"Publisher",
        description:"Repesent any data publisher"
    },
    arrow: {
        title: 'Arrow',
        description: 'Use this arrow to show a relationships between two components.'
    },
    wso2_das:{
        title:"WSO2_DAS",
        description:"WSO2 Data Analytics Server is a comprehensive enterprise data analytics platform; it fuses batch" +
        " and real-time analytics of any source of data with predictive analytics via machine learning. It supports" +
        " the demands of not just business, but Internet of Things solutions, mobile and Web apps."
    },
    wso2_iot_server:{
        title:"WSO2_IoT_Server",
        description:"With its modular, extensible, and customizable capabilities, WSO2 Internet of Things Server " +
        "(IoT Server) offers a complete, secure, open source, enterprise-grade IoT device management solution."
    }
}

var product_suggestions = {
    wso2_complex_event_processor:
       [{
           component:'database',
           description:'Persist Data Through DAS. Analise collected data.'
       }],
    wso2_machine_learner:[
        {
            component:'wso2_complex_event_processor',
            description:'Output as a data stream'
        },
        {
            component: 'database',
            description:'Do further data analytics'
        }
    ],
    ml_model:[
        {
            component:'wso2_complex_event_processor',
            description:'Output processed data stream or prediction results'
        }
    ],
}


