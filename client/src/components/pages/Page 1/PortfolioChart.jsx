import React, { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import 'bootstrap/dist/css/bootstrap.min.css';
import { ButtonGroup, Button } from "react-bootstrap";
import axios from 'axios';

const timeRanges = [
  { label: "1D", value: "1D" },
  { label: "1W", value: "1W" },
  { label: "1M", value: "1M" },
  { label: "3M", value: "3M" },
  { label: "YTD", value: "YTD" },
  { label: "1Y", value: "1Y" },
  { label: "5Y", value: "5Y" }
];

export default function PortfolioChart({ portfolioID }) {
  const [timeRange, setTimeRange] = useState("1D");
  const [chartData, setChartData] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    fetchData(timeRange);
  }, [timeRange]);

  const fetchData = async (range) => {
    try {
      const transactionsResponse = await axios.get(`http://localhost:8000/paper_trader/portfolio/get/transactions?id=${portfolioID}`);
      const transactionIDs = transactionsResponse.data;

      if (!Array.isArray(transactionIDs)) {
        throw new Error("Invalid transaction data received.");
      }

      const transactions = await Promise.all(
        transactionIDs.map(id =>
          axios.get(`http://localhost:8000/paper_trader/transaction/get?id=${id}`).then(res => res.data)
        )
      );

      const portfolioValueData = await calculatePortfolioValue(transactions, range);
      setChartData(portfolioValueData);
      setError("");
    } catch (error) {
      console.error("Error fetching data:", error);
      setError("Failed to fetch data. Please try again.");
    }
  };

  const calculatePortfolioValue = async (transactions, range) => {
    // Basic placeholder implementation to avoid undefined behavior
    // Replace with actual logic to calculate portfolio value over time

    const labels = transactions.map(transaction => transaction.gmtTime);
    const data = transactions.map(transaction => transaction.cashAmount); // Placeholder calculation

    return {
      labels: labels,
      datasets: [
        {
          label: "Portfolio Value",
          data: data,
          borderColor: "blue",
          fill: false,
        },
      ],
    };
  };

  const handleTimeRangeChange = (range) => {
    setTimeRange(range);
  };

  return (
    <div>
      {error && <p className="text-danger">{error}</p>}
      <div style={{ width: "600px", height: "400px" }}>
        <Line data={chartData} />
      </div>
      <ButtonGroup className="mt-3">
        {timeRanges.map((range) => (
          <Button
            key={range.value}
            variant={timeRange === range.value ? "primary" : "outline-primary"}
            onClick={() => handleTimeRangeChange(range.value)}
          >
            {range.label}
          </Button>
        ))}
      </ButtonGroup>
    </div>
  );
}
