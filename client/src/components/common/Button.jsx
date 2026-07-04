import React from 'react';
import './Button.css';

function Button({ 
  children, 
  type = 'button', 
  variant = 'primary', 
  size = 'medium',
  fullWidth = false,
  disabled = false,
  onClick,
  className = '',
  ...props 
}) {
  const buttonClasses = [
    'button',
    `button-${variant}`,
    `button-${size}`,
    fullWidth ? 'button-full-width' : '',
    className
  ].filter(Boolean).join(' ');

  return (
    <button
      type={type}
      className={buttonClasses}
      disabled={disabled}
      onClick={onClick}
      {...props}
    >
      {children}
    </button>
  );
}

export default Button;