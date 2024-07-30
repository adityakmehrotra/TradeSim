import React, { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import { Chart, registerables } from 'chart.js';
import 'chartjs-adapter-date-fns';
import { enUS } from 'date-fns/locale';
Chart.register(...registerables);

const StockChart = ({ ticker }) => {
    const [chartData, setChartData] = useState({});
    const [selectedRange, setSelectedRange] = useState('1D');
    const [color, setColor] = useState('green');
    const [dates, setDates] = useState([]);

    useEffect(() => {
        const fetchData = async (fromDate, toDate, timespan, multiplier) => {
            const from = fromDate ? fromDate.toISOString().split('T')[0] : new Date().toISOString().split('T')[0];
            const to = toDate ? toDate.toISOString().split('T')[0] : new Date().toISOString().split('T')[0];
            console.log(from, to);
    
            try {
                const response = await fetch(`http://localhost:8000/paper_trader/polygon/chart?ticker=${ticker}&multiplier=${multiplier}&timespan=${timespan}&from=${from}&to=${to}`);
                const data = await response.json();
    
                if (data.results && data.results.length > 0) {
                    const closingPrices = data.results.map(result => result.c);
                    const dateLabels = data.results.map(result => new Date(result.t));
                    const priceChange = closingPrices[closingPrices.length - 1] - closingPrices[0];
                    const color = priceChange >= 0 ? 'green' : 'red';
    
                    const chartData = {
                        labels: closingPrices.map((_, index) => index),
                        datasets: [
                            {
                                label: 'Price',
                                data: closingPrices,
                                borderColor: color,
                                fill: false,
                                tension: 0.1,
                            }
                        ]
                    };
                    setChartData(chartData);
                    setColor(color);
                    setDates(dateLabels);
                } else {
                    fromDate.setDate(fromDate.getDate() - 1);
                    fetchData(fromDate, toDate, timespan, multiplier);
                }
            } catch (error) {
                console.error('Error fetching stock data:', error);
                setChartData({});
                setDates([]);
            }
        };

        let multiplier = 1;
        let timespan = 'minute';
        let fromDate = new Date();
        let toDate = new Date();

        switch (selectedRange) {
            case '1D':
                fromDate.setDate(toDate.getDate() - 1);
                timespan = 'minute';
                multiplier = 10;
                break;
            case '1W':
                fromDate.setDate(toDate.getDate() - 7);
                timespan = 'hour';
                multiplier = 1;
                break;
            case '1M':
                fromDate.setMonth(toDate.getMonth() - 1);
                timespan = 'hour';
                multiplier = 1;
                break;
            case '3M':
                fromDate.setMonth(toDate.getMonth() - 3);
                timespan = 'day';
                multiplier = 1;
                break;
            case 'YTD':
                fromDate = new Date(new Date().getFullYear(), 0, 1);
                timespan = 'day';
                multiplier = 1;
                break;
            case '1Y':
                fromDate.setFullYear(toDate.getFullYear() - 1);
                timespan = 'day';
                multiplier = 1;
                break;
            case '5Y':
                fromDate.setFullYear(toDate.getFullYear() - 5);
                timespan = 'week';
                multiplier = 1;
                break;
            default:
                break;
        }

        fetchData(fromDate, toDate, timespan, multiplier);

    }, [ticker, selectedRange]);

    const options = {
        scales: {
            x: {
                type: 'linear',
                grid: {
                    display: false,
                    drawBorder: false,
                },
                border: {
                    display: false
                },
                ticks: {
                    display: false
                }
            },
            y: {
                grid: {
                    display: false,
                    drawBorder: false,
                },
                border: {
                    display: false
                },
                ticks: {
                    display: false
                }
            }
        },
        plugins: {
            tooltip: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    title: (context) => `Price: $${context[0].raw.toFixed(2)}`,
                    label: (context) => dates[context.dataIndex].toLocaleString()
                },
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#fff',
                bodyColor: '#fff',
                displayColors: false,
                position: 'nearest'
            },
            legend: {
                display: false
            },
            annotation: {
                annotations: {
                    line: {
                        type: 'line',
                        borderColor: 'rgba(0, 0, 0, 0.8)',
                        borderWidth: 1,
                        label: {
                            enabled: true,
                            content: (ctx) => `$${ctx.chart.tooltip.dataPoints[0].raw.toFixed(2)}`,
                            position: 'start'
                        }
                    }
                }
            }
        },
        elements: {
            point: {
                radius: 0
            }
        },
        maintainAspectRatio: false,
    };

    const ranges = ['1D', '1W', '1M', '3M', 'YTD', '1Y', '5Y'];

    return (
        <div style={{ position: 'relative' }}>
            <div style={{ height: '400px', marginBottom: '40px' }}>
                {chartData.datasets ? (
                    <Line data={chartData} options={options} />
                ) : (
                    <p>No data available for the selected range.</p>
                )}
            </div>
            <div style={{ display: 'flex', justifyContent: 'left', position: 'absolute', bottom: '-20px', left: '10px' }}>
                {ranges.map(range => (
                    <button
                        key={range}
                        onClick={() => setSelectedRange(range)}
                        style={{
                            backgroundColor: 'transparent',
                            color: selectedRange === range ? color : '#333',
                            border: 'none',
                            borderBottom: selectedRange === range ? `2px solid ${color}` : 'none',
                            padding: '0px 10px',
                            margin: '0 5px',
                            cursor: 'pointer',
                            fontSize: '14px'
                        }}
                    >
                        {range}
                    </button>
                ))}
            </div>
            <div style={{ height: '2px', backgroundColor: 'lightgrey', position: 'absolute', bottom: '0', left: '10px', right: '10px' }}></div>
        </div>
    );
};

export default StockChart;