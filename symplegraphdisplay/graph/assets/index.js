const autocolors = window['chartjs-plugin-autocolors'];

Chart.defaults.color = '#ffffff';

const chart = new Chart("myChart", {
    type: "line",
    plugins: [
        autocolors,
    ],
    data: {
        datasets: []
    },
    options: {
        plugins: {
            legend: {
                position: "bottom",
                align: "center"
            },
            autocolors: {
                mode: 'dataset'
            },
            zoom: {
                zoom: {
                    wheel: {
                        enabled: true,
                    },
                    pinch: {
                        enabled: true
                    },
                    mode: 'xy'
                }
            },
            decimation: {
                enabled: true,
                algorithm: 'min-max',
                samples: 1000,
            }
        },
        scales: {
            x: {
                type: 'linear'
            }
        },
        interaction: {
            intersect: false,
            mode: 'index',
            position: 'nearest'
        },
        animation: false,
        parsing: false,
    }
});


window.addEventListener("keypress", (event) => {
    if(event.key.toLowerCase() == "r" && chart.isZoomedOrPanned()) {
        chart.resetZoom();
    }
})

const packetTypes = {
    initData: "InitGraphDataPacket",
    updateData: "UpdateGraphDataPacket",
    resetData: "ResetGraphDataPacket"
}

function connect() {
    const connection = new WebSocket(`ws://${window.location.hostname}:3334`);

    connection.onopen = function () {
        console.log('Connected!');
    };
    
    connection.onerror = function (error) {
        console.log('WebSocket Error ' + error);
        connection.close();
    };
    
    connection.onclose = function (e) {
        console.log('Socket is closed. Reconnect will be attempted in 1 second.', e.reason);
        setTimeout(function() {
            connect();
        }, 1000);
    };

    connection.onmessage = function (e) {
        console.log(e.data);
        const packet = JSON.parse(e.data);
    
        switch(packet.type) {
            case packetTypes.initData:
                initData(packet.data);
                break;
    
            case packetTypes.updateData:
                addData(packet.data);
                break;

            case packetTypes.resetData:
                resetData()
                break;
                
            default:
                console.log("Unknown packet type: " + packet.type);
                break;
        }
    };
}

let currentX = 0;

function addData(data) {
    const sets = data.data;
    let maxX = currentX;
    for(const set of sets) {
        let x = currentX;
        const dataset = chart.data.datasets.find(d => d.id == set.id);
        if(!dataset) continue;
        for(const point of set.data) {
            dataset.data.push({ x: x++, y: point });
        }

        maxX = Math.max(maxX, x);
    }

    currentX = maxX;

    chart.update();
}

function initData(data) {
    const dataset = {
        id: data.id,
        label: data.label,
        data: [],
    }

    if(data.color) {
        dataset.borderColor = data.color;
        dataset.backgroundColor = data.color;
    }

    dataset.cubicInterpolationMode = 'monotone';
    dataset.indexAxis = "x"
    dataset.parsing = {
        yAxisKey: "y",
        xAxisKey: "x"
    }

    if(data.fillColor) {
        dataset.fill = {
            target: 'origin',
            above: data.fillColor,
            below: data.fillColor
        }
    }

    if(chart.data.datasets.some(d => d.id == data.id)) return;

    chart.data.datasets.push(dataset);
    chart.update();
}

function resetData() {
    console.log("Resetting data");
    chart.data.datasets = [];
    currentX = 0;
    chart.update();
}


connect();